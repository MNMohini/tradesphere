package com.bnagritech.tradesphere.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RefreshTokenResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType="Bearer";
    private Long expiresIn;
}
