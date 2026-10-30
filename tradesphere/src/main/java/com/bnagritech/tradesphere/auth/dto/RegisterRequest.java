package com.bnagritech.tradesphere.auth.dto;

import com.bnagritech.tradesphere.common.enums.UserRole;
import com.bnagritech.tradesphere.common.enums.UserStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    @NotBlank(message = "Email is Required")
    private String email;
    @NotNull(message = "Required")
    private String phoneNumber;
    //@NotNull(message = "Required")
    private String userImageUrl;
    @NotNull(message = "Role is required")
    private UserRole role;
    private UserStatus userStatus;
}
