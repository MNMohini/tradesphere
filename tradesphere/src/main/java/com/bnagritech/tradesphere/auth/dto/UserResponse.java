package com.bnagritech.tradesphere.auth.dto;

import com.bnagritech.tradesphere.common.enums.UserRole;
import com.bnagritech.tradesphere.common.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String id;
    private String userName;
    private String userId;
    private String email;
    private String employeeId;
    private String phoneNumber;
    private String profileImageUrl;
    private UserStatus status;
    private UserRole role;
    private Boolean verified;
    private String fullName;
    private Set<String> permissions;
    private Boolean accountLocked;
    private Integer failedLoginAttempts;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;

}
