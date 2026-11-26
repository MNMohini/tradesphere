package com.bnagritech.tradesphere.visit.model;

import com.bnagritech.tradesphere.imagekit.dto.MediaRef;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

import java.time.Instant;

/** One check-in or check-out: when, where, and how far from the outlet. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VisitPunch {

    private Instant time;
    private GeoJsonPoint location;
    private Double accuracyMeters;
    private MediaRef photo;
    // Straight-line distance to the outlet's saved location (null if the outlet has none)
    private Double distanceFromOutletMeters;

    // true = inside the allowed radius, false = outside (flag for manager review), null = unknown
    private Boolean withinRange;
}