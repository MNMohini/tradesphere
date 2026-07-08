package com.bnagritech.tradesphere.auth.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ChangePasswordResponse {
    private String username;
    private String userId;
    private String message;
    private Boolean success;
    private LocalDateTime changeAt;

}
