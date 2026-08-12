package com.bnagritech.tradesphere.outlet.dto;

import com.bnagritech.tradesphere.common.enums.RetailerStatus;
import com.bnagritech.tradesphere.common.enums.RetailerType;
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
    private String retailerId;
    private String shopName;
    private String phoneNumber;
    private String alternateContactNumber;
    private String email;
    private String address;
    private String territoryId;
    private String beatId;
    private String promoterId;
    private RetailerType retailerType;
    private String gstNumber;
    private String City;
    private String State;
    private String panNumber;
    private Double latitude;
    private Double longitude;
    private Double creditDays;
    private Double creditLimits;
    private RetailerStatus retailerStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private  String updatedBy;
}
