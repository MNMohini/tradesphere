package com.bnagritech.tradesphere.outlet.dto;

import com.bnagritech.tradesphere.common.enums.OutletGrade;
import com.bnagritech.tradesphere.common.enums.OutletStatus;
import com.bnagritech.tradesphere.common.enums.OutletType;
import com.bnagritech.tradesphere.outlet.model.OutletStatusHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/** Full outlet details. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OutletResponse {

    private String id;
    private String outletCode;
    private String externalCode;

    private String name;
    private String ownerName;
    private String phoneNumber;
    private String alternatePhoneNumber;
    private String email;
    private String gstNumber;
    private OutletType type;
    private OutletGrade grade;

    private OutletAddressDto address;
    private OutletLocationDto location;

    private String territoryId;
    private String beatId;
    private String distributorId;

    private OutletStatus status;
    private String statusReason;
    private List<OutletStatusHistory> statusHistory;

    private String createdBy;
    private String createdByName;
    private Instant createdAt;
    private Instant updatedAt;
}
