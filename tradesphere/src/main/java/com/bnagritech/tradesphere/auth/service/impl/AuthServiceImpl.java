package com.bnagritech.tradesphere.auth.service.impl;

import com.bnagritech.tradesphere.auth.dto.*;
import com.bnagritech.tradesphere.auth.model.User;
import com.bnagritech.tradesphere.auth.repository.UserRepository;
import com.bnagritech.tradesphere.auth.service.AuthService;
import com.bnagritech.tradesphere.common.exception.UserAlreadyExistsException;
import com.bnagritech.tradesphere.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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
        User user = User.builder()
                .userName(request.getUserName())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .employeeId(request.getEmployeeId())
                .active(true)
                .createdAt(LocalDateTime.now())
                .updateAt(LocalDateTime.now())
                .build();
        User savedUser = userRepository.save(user);

        return RegisterResponse.builder()
                .userName(savedUser.getUserName())
                .employeeId(savedUser.getEmployeeId())
                .role(savedUser.getRole())
                .build();

    }
    @Override
    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                request.getUserName(),
                request.getPassword()));
        User user = userRepository.findByUserName(request.getUserName())
                .orElseThrow(()-> new RuntimeException("Username Not Found"));

        String accessToken = jwtService.generateToken(user.getUserName());
        return LoginResponse.builder()
                .token(accessToken)
                .userName(user.getUserName())
                .role(user.getRole())
                .build();
    }

    @Override
    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request) {
        return null;
    }

    @Override
    public ResetPasswordResponse resetPassword(ResetPasswordRequest request) {
        return null;
    }

    @Override
    public void logout(String token) {

    }

    @Override
    public RegisterResponse changeUserStatus(String userId, Boolean active) {
        return null;
    }

    @Override
    public RegisterResponse changeAccountLockStatus(String userId, Boolean locked) {
        return null;
    }
}
