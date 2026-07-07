package com.bnagritech.tradesphere.auth.dto;

import com.bnagritech.tradesphere.common.enums.UserRole;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RegisterResponse {
    private String id;
    //Login
    private String userName;
    //contact details
    private String email;
    private String phoneNumber;
    //Personal
    private String firstName;
    private String lastName;
    private String profileImageUrl;
    //mapping
    private String employeeId;
    //Role
    private UserRole role;
    private Boolean active;
    private Boolean verified;
    private LocalDateTime createdAt;
}
