package com.bnagritech.tradesphere.visit.dto;

import com.bnagritech.tradesphere.common.enums.VisitOutcome;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CheckOutRequest {

    @NotNull(message = "location is required")
    @Valid
    private GeoPointDto location;

    @NotNull(message = "outcome is required")
    private VisitOutcome outcome;

    // Required for NO_ORDER, FOLLOW_UP_REQUIRED and OTHER
    @Size(max = 500)
    private String remarks;

    private Instant checkOutTime;
}