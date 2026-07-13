package com.bnagritech.tradesphere.beat.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BeatRetailer {
    @Id
    private String id;
    private String beatId;
    // Retailer Reference
    private String retailerId;
    // Route sequence Example: 1 -> Sharma Store 2 -> Gupta Traders
    private Integer sequenceNumber;
    // Priority outlet
    private Boolean mandatoryVisit;
    // Planned visit timing
    private LocalTime plannedVisitTime;
    // Expected time spent at shop
    private Integer expectedVisitDurationMinutes;
    // Active in this beat or removed
    private Boolean active;
    private String remark;
    private LocalDateTime assignedDate;
    private String assignedBy;

}
