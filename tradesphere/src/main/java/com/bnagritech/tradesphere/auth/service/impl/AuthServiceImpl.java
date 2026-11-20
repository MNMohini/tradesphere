package com.bnagritech.tradesphere.auth.service.impl;

import com.bnagritech.tradesphere.auth.dto.AuthResponse;
import com.bnagritech.tradesphere.auth.dto.ChangePasswordRequest;
import com.bnagritech.tradesphere.auth.dto.ForgotPasswordRequest;
import com.bnagritech.tradesphere.auth.dto.LoginRequest;
import com.bnagritech.tradesphere.auth.dto.MessageResponse;
import com.bnagritech.tradesphere.auth.dto.RefreshTokenRequest;
import com.bnagritech.tradesphere.auth.dto.ResetPasswordRequest;
import com.bnagritech.tradesphere.auth.dto.UpdateProfileRequest;
import com.bnagritech.tradesphere.auth.dto.UserResponse;
import com.bnagritech.tradesphere.auth.model.LoginHistory;
import com.bnagritech.tradesphere.auth.model.PasswordResetCode;
import com.bnagritech.tradesphere.auth.model.RefreshToken;
import com.bnagritech.tradesphere.auth.model.User;
import com.bnagritech.tradesphere.auth.repository.LoginHistoryRepository;
import com.bnagritech.tradesphere.auth.repository.PasswordResetCodeRepository;
import com.bnagritech.tradesphere.auth.repository.RefreshTokenRepository;
import com.bnagritech.tradesphere.auth.repository.UserRepository;
import com.bnagritech.tradesphere.auth.service.AuthService;
import com.bnagritech.tradesphere.auth.service.EmailService;
import com.bnagritech.tradesphere.auth.service.JwtService;
import com.bnagritech.tradesphere.auth.service.UserService;
import com.bnagritech.tradesphere.common.enums.LoginResult;
import com.bnagritech.tradesphere.common.enums.UserStatus;
import com.bnagritech.tradesphere.common.util.SimpleRateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);
    private static final int RESET_CODE_VALID_MINUTES = 10;
    private static final int MAX_RESET_CODE_ATTEMPTS = 5;
    private static final int MAX_RESET_REQUESTS_PER_HOUR = 3;
    private static final String FORGOT_PASSWORD_ANSWER =
            "If the account exists and has an email address, a reset code has been sent.";

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final LoginHistoryRepository loginHistoryRepository;
    private final PasswordResetCodeRepository passwordResetCodeRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final SimpleRateLimiter rateLimiter;
    private final UserService userService;

    private final SecureRandom random = new SecureRandom();
    @Value("${app.jwt.refresh-token-days:30}")
    private long refreshTokenDays = 30;

    // application.properties:  app.auth.single-device=false
    // true = a user can log in from one registered phone only (a manager can reset it)
    @Value("${app.auth.single-device:false}")
    private boolean singleDevice = false;

    // application.properties:  app.auth.log-reset-codes=false
    // true = the reset code is written to the log. For testing without a mail server ONLY.
    @Value("${app.auth.log-reset-codes:false}")
    private boolean logResetCodes = false;

    // =====================================================================
    // Login and sessions
    // =====================================================================

    @Override
    public AuthResponse login(LoginRequest r, String ipAddress, String userAgent) {
        if (!rateLimiter.tryAcquire("login:" + ipAddress, 30, Duration.ofMinutes(1))) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Too many login attempts. Try again in a minute.");
        }

        String identifier = r.getUsername().trim().toLowerCase(Locale.ROOT);
        User user = findByIdentifier(identifier);
        if (user == null) {
            record(null, identifier, LoginResult.USER_NOT_FOUND, ipAddress, r.getDeviceId(), userAgent);
            throw invalidCredentials();
        }

        Instant now = Instant.now();
        if (!user.isAccountNonLocked()) {
            record(user, identifier, LoginResult.ACCOUNT_LOCKED, ipAddress, r.getDeviceId(), userAgent);
            long minutes = Math.max(1, Duration.between(now, user.getLockedUntil()).toMinutes() + 1);
            throw new ResponseStatusException(HttpStatus.LOCKED,
                    "The account is locked after too many wrong passwords. Try again in " + minutes + " minutes.");
        }

        if (!passwordEncoder.matches(r.getPassword(), user.getPasswordHash())) {
            int attempts = user.getFailedLoginAttempts() + 1;
            if (attempts >= MAX_FAILED_ATTEMPTS) {
                user.setLockedUntil(now.plus(LOCK_DURATION));
                attempts = 0;
            }
            user.setFailedLoginAttempts(attempts);
            userRepository.save(user);
            record(user, identifier, LoginResult.INVALID_CREDENTIALS, ipAddress, r.getDeviceId(), userAgent);
            throw invalidCredentials();
        }

        // The password is right, so it is now safe to say why the account cannot be used
        if (user.getStatus() != UserStatus.ACTIVE) {
            record(user, identifier, LoginResult.ACCOUNT_LOCKED, ipAddress, r.getDeviceId(), userAgent);
            String message = user.getStatus() == UserStatus.PENDING_APPROVAL
                    ? "Your account is waiting for approval by your manager."
                    : "Your account is not active. Contact your manager.";
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, message);
        }

        if (singleDevice) {
            if (isBlank(r.getDeviceId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "deviceId is required");
            }
            if (user.getDeviceId() != null && !user.getDeviceId().equals(r.getDeviceId())) {
                record(user, identifier, LoginResult.DEVICE_MISMATCH, ipAddress, r.getDeviceId(), userAgent);
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "This account is registered to another phone. Ask your manager to reset the device.");
            }
        }

        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(now);
        if (!isBlank(r.getDeviceId())) {
            user.setDeviceId(r.getDeviceId());
            user.setDeviceName(r.getDeviceName());
        }
        if (!isBlank(r.getFcmToken())) {
            user.setFcmToken(r.getFcmToken());
        }
        userRepository.save(user);

        record(user, identifier, LoginResult.SUCCESS, ipAddress, r.getDeviceId(), userAgent);
        return issueTokens(user, r.getDeviceId(), r.getDeviceName(), ipAddress, userAgent);
    }

    @Override
    public AuthResponse refresh(RefreshTokenRequest r, String ipAddress, String userAgent) {
        RefreshToken stored = refreshTokenRepository.findByTokenHash(hash(r.getRefreshToken()))
                .orElseThrow(() -> unauthorized("Invalid refresh token"));
        User user = userRepository.findById(stored.getUserId())
                .orElseThrow(() -> unauthorized("Invalid refresh token"));

        // A token that was already swapped is being used again: it was probably stolen. End every session.
        if (stored.isRevoked()) {
            userService.revokeAllSessions(user);
            throw unauthorized("Session expired. Please log in again.");
        }
        Instant now = Instant.now();
        if (stored.getExpiresAt().isBefore(now)) {
            throw unauthorized("Session expired. Please log in again.");
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw unauthorized("Your account is not active.");
        }

        stored.setRevoked(true);
        stored.setRevokedAt(now);
        refreshTokenRepository.save(stored);

        return issueTokens(user, stored.getDeviceId(), stored.getDeviceName(), ipAddress, userAgent);
    }

    @Override
    public void logout(RefreshTokenRequest r, User currentUser) {
        refreshTokenRepository.findByTokenHash(hash(r.getRefreshToken()))
                .filter(token -> token.getUserId().equals(currentUser.getId()))
                .filter(token -> !token.isRevoked())
                .ifPresent(token -> {
                    token.setRevoked(true);
                    token.setRevokedAt(Instant.now());
                    refreshTokenRepository.save(token);
                });
    }

    @Override
    public void logoutAll(User currentUser) {
        userService.revokeAllSessions(currentUser);
    }
    // Passwords
    @Override
    public AuthResponse changePassword(ChangePasswordRequest r, User currentUser, String ipAddress, String userAgent) {
        User user = userRepository.findById(currentUser.getId()).orElseThrow(() -> unauthorized("Invalid user"));

        if (!passwordEncoder.matches(r.getCurrentPassword(), user.getPasswordHash())) {
            throw badRequest("The current password is wrong");
        }
        if (!r.getNewPassword().equals(r.getConfirmPassword())) {
            throw badRequest("newPassword and confirmPassword do not match");
        }
        if (passwordEncoder.matches(r.getNewPassword(), user.getPasswordHash())) {
            throw badRequest("The new password must be different from the current one");
        }
        requirePasswordWithoutUserName(r.getNewPassword(), user);

        user.setPasswordHash(passwordEncoder.encode(r.getNewPassword()));
        user.setPasswordChangedAt(Instant.now());
        user.setMustChangePassword(false);
        userService.revokeAllSessions(user);   // saves the user, ends every session including this one

        return issueTokens(user, r.getDeviceId(), r.getDeviceName(), ipAddress, userAgent);
    }

    @Override
    public MessageResponse forgotPassword(ForgotPasswordRequest r, String ipAddress) {

        if (!rateLimiter.tryAcquire(
                "forgot:" + ipAddress,
                10,
                Duration.ofHours(1)
        )) {
            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Too many requests. Try again later."
            );
        }

        User user = findByIdentifier(
                r.getUsername()
                        .trim()
                        .toLowerCase(Locale.ROOT)
        );

        if (user != null
                && user.getStatus() == UserStatus.ACTIVE
                && !isBlank(user.getEmail())) {

            Instant now = Instant.now();

            long recent =
                    passwordResetCodeRepository
                            .countByUserIdAndCreatedAtAfter(
                                    user.getId(),
                                    now.minus(1, ChronoUnit.HOURS)
                            );

            if (recent < MAX_RESET_REQUESTS_PER_HOUR) {

                // Only the newest reset code may work
                List<PasswordResetCode> older =
                        passwordResetCodeRepository
                                .findByUserIdAndUsedFalse(user.getId());

                older.forEach(c -> c.setUsed(true));
                passwordResetCodeRepository.saveAll(older);

                // Generate 6-digit reset code
                String code = String.format(
                        "%06d",
                        random.nextInt(1_000_000)
                );

                // Save hashed reset code in MongoDB
                passwordResetCodeRepository.save(
                        PasswordResetCode.builder()
                                .userId(user.getId())
                                .codeHash(passwordEncoder.encode(code))
                                .createdAt(now)
                                .expiresAt(
                                        now.plus(
                                                RESET_CODE_VALID_MINUTES,
                                                ChronoUnit.MINUTES
                                        )
                                )
                                .build()
                );

                // Send actual reset code to user's email
                boolean sent = emailService.sendPasswordResetCode(
                        user.getEmail(),
                        user.getFullName(),
                        code,
                        RESET_CODE_VALID_MINUTES
                );

                if (!sent) {
                    log.warn(
                            "The reset code for {} could not be emailed",
                            user.getUsername()
                    );
                }

                // Testing only
                if (logResetCodes) {
                    log.warn(
                            "the reset code for {} is {}",
                            user.getUsername(),
                            code
                    );
                }
            }
        }

        return MessageResponse.builder()
                .message(FORGOT_PASSWORD_ANSWER)
                .build();
    }

    @Override
    public MessageResponse resetPassword(ResetPasswordRequest r) {
        User user = findByIdentifier(r.getUsername().trim().toLowerCase(Locale.ROOT));
        if (user == null || user.getStatus() != UserStatus.ACTIVE) {
            throw badRequest("Invalid or expired code");
        }

        PasswordResetCode code = passwordResetCodeRepository
                .findFirstByUserIdAndUsedFalseOrderByCreatedAtDesc(user.getId())
                .orElseThrow(() -> badRequest("Invalid or expired code"));
        if (code.getExpiresAt().isBefore(Instant.now()) || code.getAttempts() >= MAX_RESET_CODE_ATTEMPTS) {
            throw badRequest("Invalid or expired code");
        }

        code.setAttempts(code.getAttempts() + 1);
        passwordResetCodeRepository.save(code);
        if (!passwordEncoder.matches(r.getCode(), code.getCodeHash())) {
            throw badRequest("Invalid or expired code");
        }
        requirePasswordWithoutUserName(r.getNewPassword(), user);

        code.setUsed(true);
        passwordResetCodeRepository.save(code);

        user.setPasswordHash(passwordEncoder.encode(r.getNewPassword()));
        user.setPasswordChangedAt(Instant.now());
        user.setMustChangePassword(false);
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userService.revokeAllSessions(user);

        return MessageResponse.builder().message("Password changed. Please log in with the new password.").build();
    }

    // =====================================================================
    // Own profile
    // =====================================================================

    @Override
    public UserResponse getMyProfile(User currentUser) {
        return userService.toResponse(
                userRepository.findById(currentUser.getId()).orElseThrow(() -> unauthorized("Invalid user")));
    }

    @Override
    public UserResponse updateMyProfile(UpdateProfileRequest r, User currentUser) {
        User user = userRepository.findById(currentUser.getId()).orElseThrow(() -> unauthorized("Invalid user"));

        if (r.getFullName() != null) {
            if (r.getFullName().isBlank()) {
                throw badRequest("fullName cannot be blank");
            }
            user.setFullName(r.getFullName().trim());
        }
        if (r.getEmail() != null) {
            String email = r.getEmail().isBlank() ? null : r.getEmail().trim().toLowerCase(Locale.ROOT);
            if (email != null && userRepository.findByEmail(email).filter(u -> !u.getId().equals(user.getId())).isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "This email is already used by another user");
            }
            user.setEmail(email);
        }
        if (r.getPhoneNumber() != null) {
            if (userRepository.findByPhoneNumber(r.getPhoneNumber()).filter(u -> !u.getId().equals(user.getId())).isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "This phone number is already used by another user");
            }
            user.setPhoneNumber(r.getPhoneNumber());
        }
        if (r.getProfileImageUrl() != null) {
            user.setProfileImageUrl(r.getProfileImageUrl());
        }
        if (r.getFcmToken() != null) {
            user.setFcmToken(r.getFcmToken());
        }

        user.setUpdatedBy(user.getId());
        try {
            return userService.toResponse(userRepository.save(user));
        } catch (DuplicateKeyException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email or phone number is already used by another user");
        }
    }

    // =====================================================================
    // Helpers
    // =====================================================================

    private AuthResponse issueTokens(User user, String deviceId, String deviceName, String ipAddress, String userAgent) {
        String rawRefreshToken = newRefreshToken();
        Instant now = Instant.now();

        refreshTokenRepository.save(RefreshToken.builder()
                .userId(user.getId())
                .tokenHash(hash(rawRefreshToken))
                .deviceId(deviceId)
                .deviceName(deviceName)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .createdAt(now)
                .expiresAt(now.plus(refreshTokenDays, ChronoUnit.DAYS))
                .build());

        return AuthResponse.builder()
                .accessToken(jwtService.generateAccessToken(user))
                .refreshToken(rawRefreshToken)
                .accessTokenExpiresInSeconds(jwtService.getAccessTokenSeconds())
                .mustChangePassword(user.isMustChangePassword())
                .user(userService.toResponse(user))
                .build();
    }

    private User findByIdentifier(String identifier) {
        Optional<User> byName = userRepository.findByUserName(identifier);
        return byName.orElseGet(() -> userRepository.findByPhoneNumber(identifier).orElse(null));
    }

    private void record(User user, String typedName, LoginResult result, String ipAddress, String deviceId, String userAgent) {
        loginHistoryRepository.save(LoginHistory.builder()
                .userId(user != null ? user.getId() : null)
                .userName(typedName)
                .result(result)
                .ipAddress(ipAddress)
                .deviceId(deviceId)
                .userAgent(userAgent)
                .attemptedAt(Instant.now())
                .build());
    }

    private void requirePasswordWithoutUserName(String password, User user) {
        if (password.toLowerCase(Locale.ROOT).contains(user.getUsername().toLowerCase(Locale.ROOT))) {
            throw badRequest("The password must not contain your user name");
        }
    }

    /** 256 random bits, text safe for URLs. Only its hash is stored. */
    private String newRefreshToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /** The same answer for an unknown user and a wrong password, so nobody can find out which users exist. */
    private ResponseStatusException invalidCredentials() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
    }

    private ResponseStatusException unauthorized(String message) {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, message);
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
