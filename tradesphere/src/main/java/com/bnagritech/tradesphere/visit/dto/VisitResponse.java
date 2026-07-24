package com.bnagritech.tradesphere.visit.dto;

import com.bnagritech.tradesphere.common.VisitType;
import com.bnagritech.tradesphere.common.enums.VisitOutCome;
import com.bnagritech.tradesphere.common.enums.VisitStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisitResponse {
    private String id;
    private String visitCode;

    private String employeeId;
    private String retailerId;
    private String beatId;
    private String territoryId;

    private LocalDateTime plannedDate;

    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;

    private VisitStatus status;
    private VisitType visitType;
    private VisitOutCome visitOutcome;

    private Integer durationInMinutes;

    private Boolean orderBooked;
    private Boolean stockVerified;
    private Boolean merchandisingDone;
    private Boolean paymentCollected;

    private String remarks;

    private String createdBy;
    private String updatedBy;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
