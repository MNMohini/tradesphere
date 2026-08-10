package com.bnagritech.tradesphere.promoter.dto;

import com.bnagritech.tradesphere.common.enums.UserRole;
import com.bnagritech.tradesphere.common.enums.UserStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PromoterRequest {

    private String promoterId;
    private String promoterName;
    private String address;
    private String employeeId;
    private String username;
    private long phoneNumber;
    private String email;
    private String territoryId;
    private String territoryName;
    private String city;
    private String state;
    private UserStatus status;
    private UserRole createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedBy;
}
