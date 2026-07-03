package com.bnagritech.tradesphere.auth.dto;

import com.bnagritech.tradesphere.common.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    private  String userName;

    @NotBlank(message = "password is required")
    private String password;

    @NotBlank(message = "Employee is required")
    private String employeeId;

    @NotBlank(message = "Role is required")
    private UserRole role;
}
