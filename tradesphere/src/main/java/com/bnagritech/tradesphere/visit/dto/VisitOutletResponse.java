package com.bnagritech.tradesphere.visit.dto;

import com.bnagritech.tradesphere.common.enums.VisitOutcome;
import com.bnagritech.tradesphere.common.enums.VisitStatus;
import com.bnagritech.tradesphere.imagekit.dto.MediaRef;
import com.bnagritech.tradesphere.outlet.model.OutletAddress;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** One outlet inside a visit. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VisitOutletResponse {

    private String outletCode;
    private String beatId;
    private String outletName;
    private String phoneNumber;
    private OutletAddress address;
    private boolean planned;
    private VisitStatus status;
    private PunchResponse checkIn;
    private PunchResponse checkOut;
    private Long durationMinutes;
    private VisitOutcome outcome;
    private String outcomeRemarks;
    private MediaRef pictureWithOutlet;
}
