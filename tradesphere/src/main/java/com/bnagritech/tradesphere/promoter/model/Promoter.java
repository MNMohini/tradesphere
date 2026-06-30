package com.bnagritech.tradesphere.promoter.model;

import com.bnagritech.tradesphere.common.UserRole;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "promoter")
public class Promoter {
    private String promoterId;
    private String promoterName;
    private String address;
    private String employeeId;
    private String phoneNumber;
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
