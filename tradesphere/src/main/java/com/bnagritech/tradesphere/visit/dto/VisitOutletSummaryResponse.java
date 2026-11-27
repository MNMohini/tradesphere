package com.bnagritech.tradesphere.visit.dto;

import com.bnagritech.tradesphere.common.enums.VisitStatus;
import com.bnagritech.tradesphere.outlet.model.OutletAddress;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VisitOutletSummaryResponse {

    private String outletCode;
    private String beatId;
    private String outletName;
    private String phoneNumber;
    private OutletAddress address;
    private boolean planned;
    private VisitStatus status;
}
