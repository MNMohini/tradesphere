package com.bnagritech.tradesphere.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ForgotPasswordResponse {
    private String message;
    private String email;
    private LocalDateTime tokenExpiryTime;
}
