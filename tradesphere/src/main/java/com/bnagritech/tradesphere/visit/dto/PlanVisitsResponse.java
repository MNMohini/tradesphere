package com.bnagritech.tradesphere.visit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlanVisitsResponse {

    private String visitId;      // VI000000001 - the visit these outlets were added to
    private int requested;       // outlets sent
    private int created;         // outlets added to the visit
    private List<VisitSummaryResponse> visits;

    // Outlets that were not added: already in a visit that day, unknown or inactive
    private List<String> skippedOutletIds;
}
