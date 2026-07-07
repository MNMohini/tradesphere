package com.bnagritech.tradesphere.auth.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ResetPasswordResponse {
    private String message;
    private LocalDateTime updatedAt;
}
