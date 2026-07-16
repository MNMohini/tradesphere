package com.bnagritech.tradesphere.auth.dto;

import com.bnagritech.tradesphere.common.enums.UserRole;
import com.bnagritech.tradesphere.common.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponse {
    private String userId;
    //Login
    private String userName;
    //contact details
    private String email;
    private String phoneNumber;
    private String message;
    //Personal
    private String employeeName;
    private UserStatus userStatus;
    private String profileImageUrl;
    //mapping
    private String employeeId;
    private Boolean verified;
    //Role
    private UserRole role;
    private UserStatus status;
    private LocalDateTime createdAt;
}
