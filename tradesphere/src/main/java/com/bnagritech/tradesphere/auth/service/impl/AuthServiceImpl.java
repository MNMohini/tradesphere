package com.bnagritech.tradesphere.auth.service.impl;
import com.bnagritech.tradesphere.auth.dto.*;
import com.bnagritech.tradesphere.auth.model.User;
import com.bnagritech.tradesphere.auth.repository.UserRepository;
import com.bnagritech.tradesphere.auth.service.AuthService;
import com.bnagritech.tradesphere.common.enums.UserStatus;
import com.bnagritech.tradesphere.common.exception.*;
import com.bnagritech.tradesphere.employee.model.Employee;
import com.bnagritech.tradesphere.employee.repository.EmployeeRepository;
import com.bnagritech.tradesphere.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

import java.util.List;
import java.util.Set;


@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmployeeRepository employeeRepository;


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
                .userName(request.getUserName())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .employeeId(employee.getEmployeeId())
                .employeeName(employee.getEmployeeName())
                .phoneNumber(employee.getPhoneNumber())
                .profileImageUrl(employee.getProfileImageUrl())
                .lastLoginAt(LocalDateTime.now())
                .failedLoginAttempts(0)
                .enabled(true)
                .permissions(Set.of())
                .accountLocked(false)
                .email(employee.getEmail())
                .build();
        User savedUser = userRepository.save(user);

        return mapToRegisterResponse(savedUser);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        authenticationManager .authenticate(new UsernamePasswordAuthenticationToken(
                request.getUserNameOrEmail(),
                request.getPassword()));
        User user = userRepository
                .findByUserNameOrEmail(request.getUserNameOrEmail(), request.getUserNameOrEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid username/email"));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UserInactiveException("Your account is not active.");
        }
        if (Boolean.TRUE.equals(user.getAccountLocked())) {
            throw new AccountLockedException(
                    "Your account is locked. please contact your administrator.");
        }
        String accessToken = jwtService.generateToken(user);
        String refreshToken =jwtService.generateRefreshToken(user);
        user.setRefreshToken(refreshToken);
        user.setLastLoginAt(LocalDateTime.now());
        user.setFailedLoginAttempts(0);
        return buildLoginResponse(user, accessToken);
    }
        @Override
        @Transactional
        public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request){
            userRepository.findByEmail(request.getEmail()
            ).orElseThrow(
                    () -> new ResourceNotFoundException("No user found with this email"));
            return ForgotPasswordResponse.builder()
                    .success(true)
                    .message("Password reset link sent to your registered email.")
                    .requestedAt(LocalDateTime.now())
                    .build();
        }

    @Override
    @Transactional
        public ResetPasswordResponse resetPassword (ResetPasswordRequest request){
        // TODO:
        // Validate reset token
        // Update password
        // Remove token

        return ResetPasswordResponse.builder()
                .success(true)
                .message("Password has been reset successfully.")
                .resetAt(LocalDateTime.now())
                .build();
        }

    @Override
    public ChangePasswordResponse changePassword(String userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(),
                user.getPassword())) {

            throw new InvalidCredentialsException("Current password is incorrect.");
        }

        if (!request.getNewPassword()
                .equals(request.getConfirmPassword())) {

            throw new PasswordNotMatchException(
                    "New password and confirm password do not match.");
        }

        if (passwordEncoder.matches(request.getNewPassword(),
                user.getPassword())) {

            throw new IllegalArgumentException(
                    "New password cannot be the same as the current password.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangeAt(LocalDateTime.now());

        userRepository.save(user);

        return ChangePasswordResponse.builder()
                .success(true)
                .message("Password changed successfully.")
                .changeAt(LocalDateTime.now())
                .build();
    }

    @Override
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        // TODO:
        // Validate refresh token
        // generate new access token
        throw new UnsupportedOperationException("Not supported yet.");

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
            return mapToUserResponse(updatedUser);

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
                .userName(user.getUsername())
                .profileImageUrl(user.getProfileImageUrl())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .userStatus(user.getStatus())
                .createdAt(LocalDateTime.now())
                .message("User registered successfully")
                .build();
    }


    private LoginResponse buildLoginResponse(User user, String accessToken) {
        return LoginResponse.builder()
                .accessToken(accessToken)
                .userId(user.getId())
                .userName(user.getUsername())
                .phoneNumber(user.getPhoneNumber())
                .employeeId(user.getEmployeeId())
                .userStatus(user.getStatus())
                .email(user.getEmail())
                .role(user.getRole())
                .lastLoginAt(LocalDateTime.now())
                .build();

    }
  
}

