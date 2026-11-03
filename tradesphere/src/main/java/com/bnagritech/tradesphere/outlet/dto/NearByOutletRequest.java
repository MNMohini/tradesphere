package com.bnagritech.tradesphere.outlet.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Query parameters for GET /api/outlets/nearby, for example:
 * /api/outlets/nearby?latitude=28.6139&longitude=77.2090&radiusMeters=500
 * Only ACTIVE outlets are returned.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NearByOutletRequest {

    @NotNull(message = "latitude is required")
    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    private Double latitude;

    @NotNull(message = "longitude is required")
    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    private Double longitude;

    // Default 500, maximum 5000
    @Min(10)
    @Max(5000)
    private Integer radiusMeters;

    // Default 20, maximum 50
    @Min(1)
    @Max(50)
    private Integer limit;
}
