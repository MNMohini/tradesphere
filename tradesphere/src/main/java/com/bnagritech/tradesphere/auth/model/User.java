package com.bnagritech.tradesphere.auth.model;

import com.bnagritech.tradesphere.common.enums.UserRole;
import com.bnagritech.tradesphere.common.enums.UserStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "users")
public class User {
    @Id
    private String id;
    //Login
    @NotBlank
    private String userName;
    @NotBlank
    private String password;
    //contact details
    @NotBlank
    private String email;
    private String phoneNumber;
    //Personal
    private String employeeName;
    private String profileImageUrl;
    //mapping
    private String employeeId;
    //Role
    private UserRole role;
    private UserStatus status;
    @Builder.Default
    private Boolean accountLocked= false;
    @Builder.Default
    private Integer failedLoginAttempts = 0;
    //audit
    private  LocalDateTime lastLoginAt;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updateAt;
    @CreatedBy
    private String createdBy;
    @LastModifiedBy
    private String updatedBy;
    //passwordReset
    private  String resetPasswordOTP;
    private LocalDateTime OTPExpires;
    private Boolean otpVerified=  false;
    private LocalDateTime accountLockedUntil;
    private LocalDateTime lastPasswordChange;
}
