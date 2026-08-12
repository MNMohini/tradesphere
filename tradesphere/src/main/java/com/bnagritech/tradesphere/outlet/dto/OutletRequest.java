package com.bnagritech.tradesphere.outlet.dto;

import com.bnagritech.tradesphere.common.enums.OutletStatus;
import com.bnagritech.tradesphere.common.enums.OutletType;
import com.bnagritech.tradesphere.common.enums.RetailerStatus;
import com.bnagritech.tradesphere.common.enums.RetailerType;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OutletRequest {

    private String outletId;
    private String outletName;
    private String phoneNumber;
    private String alternateContactNumber;
    private String email;
    private String address;
    private String territoryId;
    private String beatId;
    private String promoterId;
    private OutletType outletType;
    private OutletStatus outletStatus;
    private String gstNumber;
    private String City;
    private String State;
    private String panNumber;
    private Double latitude;
    private Double longitude;
    private Double creditDays;
    private Double creditLimits;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
