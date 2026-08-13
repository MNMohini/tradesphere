package com.bnagritech.tradesphere.retailer.dto;

import com.bnagritech.tradesphere.common.enums.RetailerStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class RetailerRequest {

    private String id;
    private String retailerId;
    private String retailerName;
    private List<String> outletIds;
    private String phoneNumber;
    private String address;
    private String panNumber;
    private RetailerStatus retailerStatus;
    private LocalDateTime createdAt;
    private String createdBy;
}
