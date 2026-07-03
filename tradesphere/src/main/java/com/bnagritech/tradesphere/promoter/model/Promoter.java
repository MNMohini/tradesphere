package com.bnagritech.tradesphere.promoter.model;

import com.bnagritech.tradesphere.common.enums.UserRole;
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
@Document(collection = "promoters")
public class Promoter {
    @Id
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
