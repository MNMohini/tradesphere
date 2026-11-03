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

    private int requested;
    private int created;
    private List<VisitSummaryResponse> visits;

    // Outlets that were not planned: already planned for that day, unknown or inactive
    private List<String> skippedOutletIds;
}