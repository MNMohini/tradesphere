package com.bnagritech.tradesphere.outlet.dto;

import com.bnagritech.tradesphere.common.enums.OutletGrade;
import com.bnagritech.tradesphere.common.enums.OutletStatus;
import com.bnagritech.tradesphere.common.enums.OutletType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** One row in an outlet list. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OutletSummaryResponse {

    private String id;
    private String outletCode;
    private String name;
    private String ownerName;
    private String phoneNumber;
    private OutletType type;
    private OutletGrade grade;
    private String city;
    private String pinCode;
    private String beatId;
    private OutletStatus status;
    private OutletLocationDto location;
}
