package com.bnagritech.tradesphere.retailer.model;


import com.bnagritech.tradesphere.auth.model.User;
import com.bnagritech.tradesphere.common.enums.RetailerStatus;
import com.bnagritech.tradesphere.common.enums.RetailerType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "retailers")
public class Retailer {
    @Id
    private String id;
    private String retailerId;
    private String shopName;
    private String ownerName;
    private String phoneNumber;
    private String alternateContactNumber;
    private String email;
    private String address;
    private String city;
    private String state;
    private String territoryId;
    private String beatId;
    private String promoterId;
    private RetailerType retailerType;
    private String gstNumber;
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
