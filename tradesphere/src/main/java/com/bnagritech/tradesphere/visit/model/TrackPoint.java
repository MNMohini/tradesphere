package com.bnagritech.tradesphere.visit.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

import java.time.Instant;

/** A GPS point recorded while the visit is in progress. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TrackPoint {

    private GeoJsonPoint location;
    private Double accuracyMeters;
    private Instant recordedAt;
}