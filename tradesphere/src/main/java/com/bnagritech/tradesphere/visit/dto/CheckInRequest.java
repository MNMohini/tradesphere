package com.bnagritech.tradesphere.visit.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Starts a visit. Send visitId to start a planned visit, or outletId to start an
 * unplanned one (one of the two is required).
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CheckInRequest {

    private String visitId;
    private String outletId;
    private String beatId;

    // UUID created by the app so a retried check-in never creates a second visit
    @Size(max = 64)
    private String clientVisitId;

    @NotNull(message = "location is required")
    @Valid
    private GeoPointDto location;

    // Time on the device (important when the check-in was done offline)
    private Instant checkInTime;
}

