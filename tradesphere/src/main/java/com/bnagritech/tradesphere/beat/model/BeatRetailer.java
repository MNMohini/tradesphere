package com.bnagritech.tradesphere.beat.model;

import java.time.LocalTime;

public class BeatRetailer {
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

}
