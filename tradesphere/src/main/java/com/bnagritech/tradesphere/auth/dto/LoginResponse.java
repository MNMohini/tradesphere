package com.bnagritech.tradesphere.auth.dto;

import com.bnagritech.tradesphere.common.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String userName;
    private UserRole role;
}
