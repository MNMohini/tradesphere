package com.bnagritech.tradesphere.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    private String token;
    private String newPassword;

}
