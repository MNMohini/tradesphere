package com.bnagritech.tradesphere.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordRequest {
    @NotBlank(message = "Current password required")
    private String currentPassword;
    @NotBlank(message = "New password required")
    private String newPassword;
    @NotBlank(message = "Confirm password required")
    private String confirmPassword;
}
