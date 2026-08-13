package com.bnagritech.tradesphere.retailer.model;


import com.bnagritech.tradesphere.common.enums.RetailerStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "retailers")
public class Retailer {
    @Id
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
