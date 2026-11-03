package com.bnagritech.tradesphere.visit.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/** Used both to send GPS points and to return them. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TrackPointDto {

    @NotNull(message = "latitude is required")
    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    private Double latitude;

    @NotNull(message = "longitude is required")
    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    private Double longitude;

    @DecimalMin("0.0")
    private Double accuracyMeters;

    // When the phone recorded the point. If missing, the server uses the current time.
    private Instant recordedAt;
}