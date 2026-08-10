package com.bnagritech.tradesphere.retailer.dto;

import com.bnagritech.tradesphere.common.enums.RetailerStatus;
import com.bnagritech.tradesphere.common.enums.RetailerType;
import lombok.Data;

import java.time.LocalDateTime;
@Data
public class RetailerRequest {

    private String retailerId;
    private String employeeId;
    private String shopName;
    private String ownerName;
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
    private String updatedBy;
}
