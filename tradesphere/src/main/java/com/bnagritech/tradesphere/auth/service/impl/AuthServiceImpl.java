package com.bnagritech.tradesphere.auth.service.impl;

import com.bnagritech.tradesphere.auth.dto.RegisterRequest;
import com.bnagritech.tradesphere.auth.dto.RegisterResponse;
import com.bnagritech.tradesphere.auth.model.User;
import com.bnagritech.tradesphere.auth.repository.UserRepository;
import com.bnagritech.tradesphere.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private  final PasswordEncoder passwordEncoder;

    @Override
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByUserName(request.getUserName())) {
            throw new RuntimeException("UserName already exists");
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


}
