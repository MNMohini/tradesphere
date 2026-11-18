package com.bnagritech.tradesphere.auth.dto;

import com.bnagritech.tradesphere.common.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Self-registration request from an employee (only when app.auth.self-registration=true).
 * The account stays PENDING_APPROVAL until the manager approves it.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "userName is required")
    @Pattern(regexp = "^[A-Za-z0-9._-]{3,30}$", message = "userName must be 3 to 30 letters, digits, . _ or -")
    private String userName;

    @NotBlank(message = "fullName is required")
    @Size(max = 100)
    private String fullName;

    @NotBlank(message = "phoneNumber is required")
    @Pattern(regexp = "^[6-9][0-9]{9}$", message = "phoneNumber must be a 10 digit mobile number")
    private String phoneNumber;

    @Email(message = "email is not valid")
    @Size(max = 150)
    private String email;

    @NotBlank(message = "password is required")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,64}$",
            message = "password must be 8 to 64 characters with a capital letter, a small letter and a digit")
    private String password;

    // Only SALES_REPRESENTATIVE or PROMOTER can self-register
    @NotNull(message = "role is required")
    private UserRole role;

    // User name of the supervisor who will approve the account
    @NotBlank(message = "managerUserName is required")
    private String managerUserName;
}
