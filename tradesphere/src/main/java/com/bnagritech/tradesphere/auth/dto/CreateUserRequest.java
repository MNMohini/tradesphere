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

import java.util.List;

/**
 * Creates a user. The server generates a temporary password and the user must change it at first login.
 * The creator must be the manager or someone above the manager.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserRequest {

    @NotBlank(message = "userName is required")
    @Pattern(regexp = "^[A-Za-z0-9._-]{3,30}$", message = "userName must be 3 to 30 letters, digits, . _ or -")
    private String userName;

    @NotBlank(message = "fullName is required")
    @Size(max = 100)
    private String fullName;

    @Email(message = "email is not valid")
    @Size(max = 150)
    private String email;

    @NotBlank(message = "phoneNumber is required")
    @Pattern(regexp = "^[6-9][0-9]{9}$", message = "phoneNumber must be a 10 digit mobile number")
    private String phoneNumber;

    @NotNull(message = "role is required")
    private UserRole role;

    // The manager the user will report to. Not needed when creating an ADMIN.
    private String managerId;

    private List<String> territoryIds;
}
