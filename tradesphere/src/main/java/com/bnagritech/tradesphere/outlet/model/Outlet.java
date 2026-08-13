package com.bnagritech.tradesphere.outlet.model;

import com.bnagritech.tradesphere.common.enums.OutletStatus;
import com.bnagritech.tradesphere.common.enums.OutletType;
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
@Document(collection = "outlets")
public class Outlet {
    @Id
    private String id;
    private String outletId;
    private String outletName;
    private String phoneNumber;
    private String alternateContactNumber;
    private String email;
    private String address;
    private String city;
    private String state;
    private String territoryId;
    private String beatId;
    private String promoterId;
    private OutletType outletType;
    private String gstNumber;
    private String panNumber;
    private Double latitude;
    private Double longitude;
    private Double creditDays;
    private Double creditLimits;
    private OutletStatus outletStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private  String updatedBy;



}
