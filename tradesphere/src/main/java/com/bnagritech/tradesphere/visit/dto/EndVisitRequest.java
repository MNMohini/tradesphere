package com.bnagritech.tradesphere.visit.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/** Ends the whole visit early. Outlets not visited become MISSED. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EndVisitRequest {

    @NotNull(message = "location is required")
    @Valid
    private GeoPointDto location;

    @Size(max = 500)
    private String remarks;

    private Instant checkOutTime;
}
