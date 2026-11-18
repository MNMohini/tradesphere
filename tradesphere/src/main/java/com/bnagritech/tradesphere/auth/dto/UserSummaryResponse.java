package com.bnagritech.tradesphere.auth.dto;

import com.bnagritech.tradesphere.common.enums.UserRole;
import com.bnagritech.tradesphere.common.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/** One row in a user list. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserSummaryResponse {

    private String id;
    private String userName;
    private String fullName;
    private String employeeId;
    private String phoneNumber;
    private UserRole role;
    private String managerId;
    private UserStatus status;
    private Instant lastLoginAt;
}
