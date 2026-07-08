package com.bnagritech.tradesphere.auth.dto;

import com.bnagritech.tradesphere.common.enums.UserRole;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private String userName;
    private String userId;
    private String fullName;
    private String email;
    private String profileImageUrl;
    private UserRole role;
    private boolean active;
}
