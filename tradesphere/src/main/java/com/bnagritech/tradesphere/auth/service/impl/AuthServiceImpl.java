package com.bnagritech.tradesphere.auth.service.impl;

import com.bnagritech.tradesphere.auth.dto.*;
import com.bnagritech.tradesphere.auth.model.User;
import com.bnagritech.tradesphere.auth.repository.UserRepository;
import com.bnagritech.tradesphere.auth.service.AuthService;
import com.bnagritech.tradesphere.auth.service.OTPService;
import com.bnagritech.tradesphere.common.enums.UserStatus;
import com.bnagritech.tradesphere.common.exception.*;
import com.bnagritech.tradesphere.employee.model.Employee;
import com.bnagritech.tradesphere.employee.repository.EmployeeRepository;
import com.bnagritech.tradesphere.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;


@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final OTPService  otpService;
    private final AuthenticationManager authenticationManager;
    private final EmployeeRepository employeeRepository;
     private static final int MAX_FAILED_LOGIN_ATTEMPTS = 5;
     private static final int ACCOUNT_LOCKED_DURATION_MINUTES=15;
     private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public RegisterResponse createUserAccount(RegisterRequest request) {

         Employee employee= employeeRepository.findByEmployeeId(request.getEmployeeId())
                .orElseThrow(()-> new ResourceNotFoundException(
                        "Employee not found with employeeId "
                                +request.getEmployeeId()));

        if (userRepository.existsByUserName(request.getUserName())) {
            throw new UserAlreadyExistsException(
                    "UserName already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException(
                    "User already exists with this email");
        }
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new UserAlreadyExistsException(
                    "User already exists with this phone number");
        }
        if (userRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new UserAlreadyExistsException(
                    "User already exists with this employee id");
        }

        User user = User.builder()
                .userName(employee.getUserName())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .employeeId(employee.getEmployeeId())
                .phoneNumber(employee.getPhoneNumber())
                .profileImageUrl(employee.getProfileImageUrl())
                .employeeName(employee.getEmployeeName())
                .lastLoginAt(LocalDateTime.now())
                .status(UserStatus.ACTIVE)
                .failedLoginAttempts(0)
                .accountLocked(false)
                .otpVerified(false)
                .lastLoginAt(LocalDateTime.now())
                .email(employee.getEmail())
                .createdAt(LocalDateTime.now())
                .updateAt(LocalDateTime.now())
                .build();
        User savedUser = userRepository.save(user);

        return mapToRegisterResponse(savedUser);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository
                .findByUserNameOrEmail(
                        request.getUserNameOrEmail(),
                        request.getUserNameOrEmail())
                .orElseThrow(() ->
                        new InvalidCredentialsException("Invalid username/email or password"));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UserInactiveException("Your account is not active.");
        }
        if (Boolean.TRUE.equals(user.getAccountLocked())) {
            if (user.getAccountLockedUntil() != null &&
                    user.getAccountLockedUntil().isAfter(LocalDateTime.now())) {
                throw new AccountLockedException
                        ("Your account is locked. Please contact to the administrator.");
            }
            user.setAccountLocked(false);
            user.setAccountLockedUntil(null);
            user.setFailedLoginAttempts(0);
        }

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.getUserNameOrEmail(), request.getPassword()));
        }
        catch (AuthenticationException ex) {
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
            if (user.getFailedLoginAttempts() >= MAX_FAILED_LOGIN_ATTEMPTS) {
                user.setAccountLocked(true);
                user.setAccountLockedUntil(
                        LocalDateTime.now().plusMinutes(ACCOUNT_LOCKED_DURATION_MINUTES));
                }
            userRepository.save(user);
            throw new InvalidCredentialsException("Invalid username/email or password.");}
        user.setFailedLoginAttempts(0);
        user.setAccountLocked(false);
        user.setAccountLockedUntil(null);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        String accessToken = jwtService.generateToken(
                new org.springframework.security.core.userdetails.User(
                        user.getUserName(),
                        user.getPassword(),
                        Collections.singletonList(
                                new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))));
        return buildLoginResponse(user, accessToken);
    }
        @Override
        @Transactional
        public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request){
            User user = userRepository.findByPhoneNumber(
                    request.getPhoneNumber()
            ).orElseThrow(
                    () -> new ResourceNotFoundException("No user found with this phone number"));
            otpService.sendOTP(user.getPhoneNumber());
            return ForgotPasswordResponse.builder()
                    .message("Password reset link sent to your registered Phone Number.")
                    .requestedAt(LocalDateTime.now())
                    .build();
        }

    @Override
    @Transactional
    public VerifyOTPResponse verifyOTP(VerifyOTPRequest request) {
        User user = userRepository.findByPhoneNumber(request.getPhoneNumber())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found."));

        boolean verified = otpService.verifyOtp(
                request.getPhoneNumber(),
                request.getOTP());

        if (!verified) {
            throw new InvalidOTPException("Invalid OTP.");
        }

        user.setOtpVerified(true);
        userRepository.save(user);

        return VerifyOTPResponse.builder()
                .verified(true)
                .message("OTP verified successfully.")
                .build();
    }

    @Override
    @Transactional
        public ResetPasswordResponse resetPassword (ResetPasswordRequest request){
            User user = userRepository
                    .findByPhoneNumber(request.getPhoneNumber())
                    .orElseThrow(() ->
                            new ResourceNotFoundException("User not found.")
                    );
            if (!Boolean.TRUE.equals(user.getOtpVerified())) {
                throw new InvalidOTPException("Invalid OTP.");
            }
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                throw new PasswordNotMatchException("Passwords do not match.");
            }
            //clear the after use
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            user.setOtpVerified(false);
            user.setLastPasswordChange(LocalDateTime.now());
            userRepository.save(user);
            return ResetPasswordResponse.builder()
                    .success(true)
                    .message("Password reset successfully")
                    .build();
        }

        @Override
        public ChangePasswordResponse changePassword (ChangePasswordRequest request){
            //get logged-in username from jwt security context
            Authentication authentication = SecurityContextHolder
                    .getContext()
                    .getAuthentication();
            String userName = authentication.getName();
            User user = userRepository.findByUserName(userName)
                    .orElseThrow(
                            () -> new ResourceNotFoundException("User not found")
                    );
            //check old password
            if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
                throw new ResourceNotFoundException("Old password doesn't match");
            }
            // check new password and confirm password
            if (request.getNewPassword().equals(request.getConfirmPassword())) {
                throw new ResourceNotFoundException("Passwords didn't match");
            }
            if(passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
                throw new BadRequestException(
                        "New passwords can't be the same as the current password.");
            }
            user.setPassword(passwordEncoder.encode(
                    request.getNewPassword()));
            userRepository.save(user);
            return ChangePasswordResponse.builder()
                    .message("Password changed successfully")
                    .changeAt(LocalDateTime.now())
                    .build();
        }

        @Override
        public UserResponse updateUserStatus (String userId, UpdateUserStatusRequest request){
            // Find user by id
            User user = userRepository.findById(userId)
                    .orElseThrow(
                            () -> new RuntimeException("User not found"));
            // Update active status
            user.setStatus(request.getStatus());
            // Save changes
            User updatedUser =
                    userRepository.save(user);
            // Return response
            return UserResponse.builder()
                    .userId(updatedUser.getId())
                    .employeeId(updatedUser.getEmployeeId())
                    .userName(updatedUser.getUserName())
                    .email(updatedUser.getEmail())
                    .phoneNumber(updatedUser.getPhoneNumber())
                    .role(updatedUser.getRole())
                    .status(updatedUser.getStatus())
                    .build();

        }
        @Override
        public void logout (String token){
            SecurityContextHolder.clearContext();
        }

        @Override
        public List<UserResponse> getAllUsers () {
            return userRepository.findAll()
                    .stream()
                    .map(this::mapToUserResponse)
                    .toList();
        }
        @Override
        public UserResponse getUserByUserId (String userId){
            User user = userRepository
                    .findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
            return mapToUserResponse(user);

        }
    private UserResponse mapToUserResponse(
            User user) {
        return UserResponse.builder()
                .userId(user.getId())
                .employeeId(user.getEmployeeId())
                .userName(user.getUserName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .status(user.getStatus())
                .build();
    }
    private RegisterResponse mapToRegisterResponse(
            User user) {
        return RegisterResponse.builder()
                .userId(user.getId())
                .employeeId(user.getEmployeeId())
                .userName(user.getUserName())
                .employeeName(user.getEmployeeName())
                .profileImageUrl(user.getProfileImageUrl())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(LocalDateTime.now())
                .message("User registered successfully")
                .build();
    }


    private LoginResponse buildLoginResponse(User user, String accessToken) {
        return LoginResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .userId(user.getId())
                .expiresIn(jwtService.getTokenExpiryTime())
                .userName(user.getUserName())
                .profileImageUrl(user.getProfileImageUrl())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .message("User registered successfully")
                .build();

    }
    private String generateOtp() {
        return String.format("%06d", secureRandom.nextInt(1000000));
    }

}

