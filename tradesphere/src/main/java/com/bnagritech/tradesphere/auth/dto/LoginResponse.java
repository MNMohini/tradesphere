package com.bnagritech.tradesphere.auth.dto;

import com.bnagritech.tradesphere.common.enums.UserRole;
import com.bnagritech.tradesphere.common.enums.UserStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String accessToken;
    private String userName;
    private String employeeId;
    private UserStatus userStatus;
    private String phoneNumber;
    private UserRole role;
    private String userId;
    private String email;
    private LocalDateTime lastLoginAt;
}
