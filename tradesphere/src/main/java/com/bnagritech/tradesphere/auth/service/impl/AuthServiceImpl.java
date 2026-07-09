package com.bnagritech.tradesphere.auth.service.impl;

import com.bnagritech.tradesphere.auth.dto.*;
import com.bnagritech.tradesphere.auth.model.User;
import com.bnagritech.tradesphere.auth.repository.UserRepository;
import com.bnagritech.tradesphere.auth.service.AuthService;
import com.bnagritech.tradesphere.common.exception.ResourceNotFoundException;
import com.bnagritech.tradesphere.common.exception.UserAlreadyExistsException;
import com.bnagritech.tradesphere.common.exception.UserInactiveException;
import com.bnagritech.tradesphere.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    private final AuthenticationManager authenticationManager;

    @Override
    public RegisterResponse register(RegisterRequest request) {
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
        User user = User.builder()
                .userName(request.getUserName())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .employeeId(request.getEmployeeId())
                .phoneNumber(request.getPhoneNumber())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .lastLoginAt(LocalDateTime.now())
                .active(true)
                .email(request.getEmail())
                .createdAt(LocalDateTime.now())
                .updateAt(LocalDateTime.now())
                .build();
        User savedUser = userRepository.save(user);

        return RegisterResponse.builder()
                .userId(savedUser.getId())
                .userName(savedUser.getUserName())
                .message("User registered successfully")
                .build();

    }
    @Override
    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                request.getUserNameOrEmail(),
                request.getPassword()));
        User user = userRepository.findByUserNameOrEmail(
                request.getUserNameOrEmail(),
                        request.getUserNameOrEmail())
                .orElseThrow(()-> new ResourceNotFoundException("Invalid Username or email"));
        if(!user.getActive()){
            throw new UserInactiveException("Your account is inactive.Contact to admin");
        }

        String token = jwtService.generateToken(String.valueOf(user));
        return LoginResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .message("Login successfully")
                .token(token)
                .active(user.getActive())
                .userName(user.getUserName())
                .role(user.getRole())
                .build();
    }

    @Override
    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request) {
        User user= userRepository.findByEmail(
                request.getEmail()
        ).orElseThrow(
                ()-> new ResourceNotFoundException("Email not found"));
        String token = UUID.randomUUID().toString();

        user.setResetPasswordToken(token);
        user.setResetPasswordTokenExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);
        return ForgotPasswordResponse.builder()
                .success(true)
                .token(token)
                .message("Reset Password token generated Successfully")
                .build();
    }

    @Override
    public ResetPasswordResponse resetPassword(ResetPasswordRequest request) {
            User user= userRepository
                    .findByResetPasswordToken(request.getToken())
                    .orElseThrow(
                            ()-> new ResourceNotFoundException("Invalid Token")
                    );
            if (user.getResetPasswordTokenExpiry().plusMinutes(15).isBefore(LocalDateTime.now())) {
                throw new ResourceNotFoundException("Reset token expired");
            }
            //clear the after use
            user.setResetPasswordToken(null);
            user.setResetPasswordTokenExpiry(null);
            userRepository.save(user);
        return ResetPasswordResponse.builder()
                .success(true)
                .message("Password reset successfully")
                .build();
    }

    @Override
    public ChangePasswordResponse changePassword(ChangePasswordRequest request) {
        //get logged-in username from jwt security context
        String userName = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();
        User user = userRepository.findByUserName(userName)
                .orElseThrow(
                        ()-> new ResourceNotFoundException("User not found")
                );
        //check old password
        if(!passwordEncoder.matches(request.getOldPassword(), user.getPassword())){
            throw new ResourceNotFoundException("Old password doesn't match");
        }
        // check new password and confirm password
        if (request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ResourceNotFoundException("Passwords didn't match");
        }
        user.setPassword(passwordEncoder.encode(
                request.getNewPassword()));
        userRepository.save(user);
        return ChangePasswordResponse.builder()
                .userId(user.getId())
                .username(user.getUserName())
                .message("Password changed successfully")
                .success(true)
                .changeAt(LocalDateTime.now())
                .build();
    }

    @Override
    public UserResponse updateUserStatus(String userId, UpdateUserStatusRequest request) {
        // Find user by id
        User user = userRepository.findById(userId)
                .orElseThrow(
                        () -> new RuntimeException("User not found"));
        // Update active status
        user.setActive(request.isActive());
        // Save changes
        User updatedUser =
                userRepository.save(user);
        // Return response
        return UserResponse.builder()
                .userId(updatedUser.getId())
                .employeeId(updatedUser.getEmployeeId())
                .userName(updatedUser.getUserName())
                .email(updatedUser.getEmail())
                .phoneNumber(Long.parseLong(updatedUser.getPhoneNumber()))
                .role(updatedUser.getRole())
                .active(updatedUser.getActive())
                .build();

    }
    @Override
    public void logout(String token) {
        SecurityContextHolder.clearContext();
    }
    @Override
    public RegisterResponse changeUserStatus(String userId, Boolean active) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setActive(active);
        User updatedUser = userRepository.save(user);
        return RegisterResponse.builder()
                .userId(updatedUser.getId())
                .userName(updatedUser.getUserName())
                .email(updatedUser.getEmail())
                .role(updatedUser.getRole())
                .message(active ? "User activated successfully":"User deactivated successfully")
                .build();
    }
    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToUserResponse)
                .toList();
    }
    @Override
    public UserResponse getUserById(String userId) {
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
                .phoneNumber(Long.parseLong(user.getPhoneNumber()))
                .role(user.getRole())
                .active(user.getActive())
                .build();

    }

}
