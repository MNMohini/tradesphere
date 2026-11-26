package com.bnagritech.tradesphere.visit.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Check-in at one outlet.
 *  - visitId given:   check in at this outlet inside that visit (an outlet not in the plan is added).
 *  - visitId missing: today's open visit is used; if there is none, a new unplanned visit is created.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CheckInRequest {

    private String visitId;              // VI000000001 or the Mongo id (optional)

    @NotBlank(message = "outletCode is required")
    private String outletCode;           // outlet code or outlet id

    private String photoFileId;          // fileId of the photo uploaded to ImageKit (VisitImages)
    private String beatId;

    // UUID created by the app so a retried walk-in never creates a second visit
    @Size(max = 64)
    private String clientVisitId;

    @NotNull(message = "location is required")
    @Valid
    private GeoPointDto location;

    // Time on the device (important when the check-in was done offline)
    private Instant checkInTime;
}
