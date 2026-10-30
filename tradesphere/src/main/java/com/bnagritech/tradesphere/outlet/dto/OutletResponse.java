package com.bnagritech.tradesphere.outlet.dto;

import com.bnagritech.tradesphere.common.enums.OutletStatus;
import com.bnagritech.tradesphere.common.enums.OutletType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutletResponse {

    private String outletId;
    private String outletName;
    private String mobileNumber;
    private String alternateContactNumber;
    private String email;
    private String address;
    private String territoryId;
    private String beatId;
    private OutletType outletType;
    private String City;
    private String State;
    private String pinCode;
    private Double latitude;
    private Double longitude;
    private LocalDateTime updatedAt;
    private OutletStatus outletStatus;
    private String createdBy;
    private  String updatedBy;
}
