package com.bnagritech.tradesphere.auth.dto;

import java.time.LocalDateTime;

public class ForgotPasswordResponse {
    private String message;
    private String email;
    private LocalDateTime tokenExpiryTime;
}
