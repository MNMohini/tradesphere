package com.bnagritech.tradesphere.security.config;

import com.bnagritech.tradesphere.auth.model.User;
import com.bnagritech.tradesphere.auth.repository.UserRepository;
import com.bnagritech.tradesphere.common.enums.UserRole;
import com.bnagritech.tradesphere.common.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
@Configuration
@RequiredArgsConstructor
public class AdminInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner initAdmin() {
        return args -> {
            if (!userRepository.existsByUserName("Manoj01")) {
                User admin = User.builder()
                        .userName("Manoj01")
                        .employeeId("E-11680")
                        .email("manoj@123")
                        .password(passwordEncoder.encode("manoj@bngroup"))
                        .role(UserRole.ADMIN)
                        .status(UserStatus.ACTIVE)
                        .enabled(true)
                        .accountLocked(false)
                        .failedLoginAttempts(0)
                        .build();
                userRepository.save(admin);

                System.out.println("Admin user created successfully.");
            }
        };
    }
}
