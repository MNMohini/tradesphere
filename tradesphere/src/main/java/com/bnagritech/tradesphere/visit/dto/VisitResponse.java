package com.bnagritech.tradesphere.visit.dto;

import com.bnagritech.tradesphere.common.enums.VisitOutcome;
import com.bnagritech.tradesphere.common.enums.VisitStatus;
import com.bnagritech.tradesphere.visit.model.VisitStatusHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/** Full visit details: the whole story of one visit. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VisitResponse {

    private String id;
    private String clientVisitId;

    private String userId;
    private String userName;

    private String outletId;
    private String outletName;
    private GeoPointDto outletLocation;
    private String beatId;

    private LocalDate visitDay;
    private boolean planned;
    private VisitStatus status;

    private PunchResponse checkIn;
    private PunchResponse checkOut;
    private Long durationMinutes;

    private VisitOutcome outcome;
    private String outcomeRemarks;
    private String cancellationReason;

    private int orderCount;

    private List<TrackPointDto> trackPoints;
    private List<VisitStatusHistory> statusHistory;

    private Instant createdAt;
    private Instant updatedAt;
}