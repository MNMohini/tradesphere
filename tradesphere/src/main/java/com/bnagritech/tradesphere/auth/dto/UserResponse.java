package com.bnagritech.tradesphere.auth.dto;

import com.bnagritech.tradesphere.common.enums.UserRole;
import com.bnagritech.tradesphere.common.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/** Full user details. The password hash and tokens are never included. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {

    private String id;
    private String userName;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String profileImageUrl;

    private UserRole role;
    private String managerId;
    private List<String> territoryIds;

    private UserStatus status;
    private String statusReason;
    private boolean mustChangePassword;

    private Instant lastLoginAt;
    private Instant createdAt;
    private Instant updatedAt;
}
