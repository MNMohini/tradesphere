package com.bnagritech.tradesphere.visit.dto;

import com.bnagritech.tradesphere.common.enums.VisitOutcome;
import com.bnagritech.tradesphere.common.enums.VisitStatus;
import com.bnagritech.tradesphere.imagekit.dto.MediaRef;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VisitorAttendanceItemResponse {

    private String visitId;
    private LocalDate visitDay;
    private String visitorName;
    private String managerName;
    private String outletCode;
    private String outletName;
    private MediaRef photo;
    private Instant checkInTime;
    private Instant checkOutTime;
    private Long durationMinutes;
    private VisitOutcome outcome;
    private String remarks;
    private VisitStatus status;
}
