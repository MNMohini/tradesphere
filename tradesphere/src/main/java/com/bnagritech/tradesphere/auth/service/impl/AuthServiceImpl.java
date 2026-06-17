package com.bnagritech.tradesphere.auth.service.impl;

import com.bnagritech.tradesphere.auth.dto.LoginRequest;
import com.bnagritech.tradesphere.auth.dto.LoginResponse;
import com.bnagritech.tradesphere.auth.dto.RegisterRequest;
import com.bnagritech.tradesphere.auth.dto.RegisterResponse;
import com.bnagritech.tradesphere.auth.model.User;
import com.bnagritech.tradesphere.auth.repository.UserRepository;
import com.bnagritech.tradesphere.auth.service.AuthService;
import com.bnagritech.tradesphere.common.exception.UserAlreadyExistsException;
import com.bnagritech.tradesphere.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    @Override
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByUserName(request.getUserName())) {
            throw new UserAlreadyExistsException(
                    "UserName already exists"
            );
        }
        User user = User.builder()
                .userName(request.getUserName())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .employeeId(request.getEmployeeId())
                .active(true)
                .createdAt(LocalDateTime.now())
                .updateBy(LocalDateTime.now())
                .build();
        User savedUser = userRepository.save(user);

        return RegisterResponse.builder()
                .id(savedUser.getId())
                .userName(savedUser.getUserName())
                .role(savedUser.getRole())
                .employeeId(savedUser.getEmployeeId())
                .active(savedUser.getActive())
                .build();
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository
                .findByUserName(request.getUserName())
                .orElseThrow(
                        () -> new RuntimeException(
                                "Invalid UserName"
                        )
                );
        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword() ))
        {
            throw new RuntimeException(
                    "Invalid password"
             );
        }
        String token = jwtService.generateToken(
                user.getUserName()
        );
        return LoginResponse.builder()
                .accessToken(token)
                .userName(user.getUserName())
                .role(user.getRole())
                .build();
    }
}
