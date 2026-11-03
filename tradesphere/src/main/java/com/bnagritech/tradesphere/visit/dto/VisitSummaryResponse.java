package com.bnagritech.tradesphere.visit.dto;

import com.bnagritech.tradesphere.common.enums.VisitOutcome;
import com.bnagritech.tradesphere.common.enums.VisitStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

/** One row in a visit list. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VisitSummaryResponse {

    private String id;
    private String userId;
    private String userName;
    private String outletId;
    private String outletName;
    private String beatId;
    private LocalDate visitDay;
    private boolean planned;
    private VisitStatus status;
    private Instant checkInTime;
    private Instant checkOutTime;
    private Long durationMinutes;
    private VisitOutcome outcome;
    private Boolean checkInWithinRange;
}