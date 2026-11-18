package com.bnagritech.tradesphere.auth.dto;

import com.bnagritech.tradesphere.common.enums.UserStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Approve a registration or reactivate (ACTIVE), reject or deactivate (INACTIVE), or block (SUSPENDED).
 * A reason is required for INACTIVE and SUSPENDED.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserStatusRequest {

    @NotNull(message = "status is required")
    private UserStatus status;

    @Size(max = 500)
    private String remarks;
}
