package com.bnagritech.tradesphere.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Returned by login, refresh and change-password. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {

    private String accessToken;
    private String refreshToken;

    @Builder.Default
    private String tokenType = "Bearer";

    private long accessTokenExpiresInSeconds;

    // true: the app must send the user to "change password" before anything else
    private boolean mustChangePassword;

    private UserResponse user;
}
