package com.bnagritech.tradesphere.visit.model;

import com.bnagritech.tradesphere.common.enums.VisitOutcome;
import com.bnagritech.tradesphere.common.enums.VisitStatus;
import com.bnagritech.tradesphere.imagekit.dto.MediaRef;
import com.bnagritech.tradesphere.outlet.model.OutletAddress;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** One outlet inside a visit: what happened at that shop. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VisitOutlet {

    private String outletCode;
    private String beatId;
    private String outletName;
    private String phoneNumber;
    private OutletAddress address;
    private boolean planned;             // false = added at check-in (walk-in)
    private VisitStatus status;          // PLANNED, IN_PROGRESS, COMPLETED, MISSED, CANCELLED
    private VisitPunch checkIn;
    private VisitPunch checkOut;
    private Long durationMinutes;
    private VisitOutcome outcome;
    private String outcomeRemarks;
    private MediaRef pictureWithOutlet;  // photo taken at check-in (ImageKit, VisitImages folder)
}
