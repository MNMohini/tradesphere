package com.bnagritech.tradesphere.outlet.dto;

import com.bnagritech.tradesphere.common.enums.OutletStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Approve (ACTIVE), reject (REJECTED), deactivate (INACTIVE), reactivate (ACTIVE),
 * or resubmit a rejected outlet (PENDING_APPROVAL). A reason is required for REJECTED and INACTIVE.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateOutletStatusRequest {

    @NotNull(message = "status is required")
    private OutletStatus status;

    @Size(max = 500)
    private String remarks;
}
