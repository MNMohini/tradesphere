package com.bnagritech.tradesphere.outlet.model;

import com.bnagritech.tradesphere.common.enums.OutletStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OutletStatusHistory {

    private OutletStatus fromStatus;
    private OutletStatus toStatus;
    private String updatedBy;
    private String remarks;
    private Instant updatedAt;
}
