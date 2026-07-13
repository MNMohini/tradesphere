package com.bnagritech.tradesphere.auth.dto;

import com.bnagritech.tradesphere.common.enums.UserRole;
import com.bnagritech.tradesphere.common.enums.UserStatus;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private String refreshToken;
    private String tokenType;
    private String userName;
    private String userId;
    private String fullName;
    private String email;
    private String profileImageUrl;
    private UserRole role;
    private UserStatus status;
    private String message;
}
