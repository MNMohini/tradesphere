package com.bnagritech.tradesphere.auth.dto;

import com.bnagritech.tradesphere.common.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    //login
    @NotBlank(message = "Username is required")
    private  String userName;
    @NotBlank(message = "password is required")
    private String password;

    //contact
    @NotBlank(message = "Required")
    private String email;
    @NotBlank(message = "Required")
    private String phoneNumber;

    //mapping
    @NotBlank(message = "Employee is required")
    private String employeeId;

    //personal
    @NotBlank(message = "Required")
    private String firstName;
    @NotBlank(message = "Required")
    private String lastName;
    @NotBlank(message = "Required")
    private String profileImageUrl;

    @NotBlank(message = "Role is required")
    private UserRole role;
}
