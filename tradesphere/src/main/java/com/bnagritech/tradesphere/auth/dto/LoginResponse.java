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
    private String refreshToken;
    private String tokenType = "Bearer";
    private String userName;
    private String employeeId;
    private UserStatus userStatus;
    private Long expiresIn;
    private String userId;
    private String email;
    private UserRole role;
    private UserStatus status;
    private LocalDateTime lastLoginAt;
}
