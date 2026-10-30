package com.bnagritech.tradesphere.outlet.dto;

import com.bnagritech.tradesphere.common.enums.OutletStatus;
import com.bnagritech.tradesphere.common.enums.OutletType;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OutletRequest {

    private String outletId;
    private String outletName;
    private String mobileNumber;
    private String alternateContactNumber;
    private String email;
    private String address;
    private String territoryId;
    private String beatId;
    private OutletType outletType;
    private OutletStatus outletStatus;
    private String City;
    private String State;
    private String pinCode;
    private Double latitude;
    private Double longitude;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
