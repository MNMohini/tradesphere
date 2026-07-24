package com.bnagritech.tradesphere.visit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisitSummaryResponse {

    private long totalVisits;
    private long completedVisits;
    private long pendingVisits;
    private long cancelledVisits;
    private long missedVisits;
}
