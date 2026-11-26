package com.bnagritech.tradesphere.visit.dto;

import com.bnagritech.tradesphere.imagekit.dto.MediaRef;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PunchResponse {

    private Instant time;
    private GeoPointDto location;
    private Double distanceFromOutletMeters;
    private Boolean withinRange;
    private MediaRef photo;
}