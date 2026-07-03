package com.bnagritech.tradesphere.promoter.dto;

import com.bnagritech.tradesphere.common.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromoterResponse {

    private String id;
    private String promoterId;
    private String promoterName;
    private String address;
    private String employeeId;
    private long phoneNumber;
    private String email;
    private String territoryId;
    private String territoryName;
    private String city;
    private String state;
    private String status;
    private UserRole createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedBy;
}
