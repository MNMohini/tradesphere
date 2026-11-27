package com.bnagritech.tradesphere.auth.service.impl;

import com.bnagritech.tradesphere.auth.model.User;
import com.bnagritech.tradesphere.auth.repository.LoginHistoryRepository;
import com.bnagritech.tradesphere.auth.repository.RefreshTokenRepository;
import com.bnagritech.tradesphere.auth.repository.UserRepository;
import com.bnagritech.tradesphere.auth.service.EmailService;
import com.bnagritech.tradesphere.common.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private LoginHistoryRepository loginHistoryRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void profileResolvesManagerStoredAsUsername() {
        User manager = User.builder()
                .id("manager-id")
                .userName("akhand")
                .fullName("Akhand Upadhyay")
                .role(UserRole.AREA_MANAGER)
                .build();
        User employee = User.builder()
                .id("employee-id")
                .userName("sales-rep")
                .managerId("akhand")
                .build();
        when(userRepository.findById("akhand")).thenReturn(Optional.empty());
        when(userRepository.findByUserName("akhand")).thenReturn(Optional.of(manager));

        var response = userService.toResponse(employee);

        assertEquals("Akhand Upadhyay", response.getManagerName());
        assertEquals(UserRole.AREA_MANAGER, response.getManagerRole());
    }

    @Test
    void profileResolvesManagerStoredAsMongoId() {
        User manager = User.builder()
                .id("6b0542add0d21dcd427ea8a0")
                .userName("akhand")
                .fullName("Akhand Upadhyay")
                .role(UserRole.AREA_MANAGER)
                .build();
        User employee = User.builder()
                .id("employee-id")
                .userName("sales-rep")
                .managerId(manager.getId())
                .build();
        when(userRepository.findById(manager.getId())).thenReturn(Optional.of(manager));

        var response = userService.toResponse(employee);

        assertEquals("Akhand Upadhyay", response.getManagerName());
        assertEquals(UserRole.AREA_MANAGER, response.getManagerRole());
    }
}
