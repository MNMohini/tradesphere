package com.bnagritech.tradesphere.auth.dto;

import jakarta.validation.constraints.NotBlank;

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
