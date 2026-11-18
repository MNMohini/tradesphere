package com.bnagritech.tradesphere.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordRequest {

    @NotBlank(message = "username is required")
    @Size(max = 100)
    private String username;

    // The 6 digit code that was emailed
    @NotBlank(message = "code is required")
    @Pattern(regexp = "^[0-9]{6}$", message = "code must be 6 digits")
    private String code;

    @NotBlank(message = "newPassword is required")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,64}$",
            message = "newPassword must be 8 to 64 characters with a capital letter, a small letter and a digit")
    private String newPassword;
}
