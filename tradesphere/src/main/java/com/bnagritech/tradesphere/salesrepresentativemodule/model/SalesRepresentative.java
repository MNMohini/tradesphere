package com.bnagritech.tradesphere.salesrepresentativemodule.model;

import com.bnagritech.tradesphere.common.enums.UserRole;
import com.bnagritech.tradesphere.common.enums.UserStatus;
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
@Document(collection= "SR")
public class SalesRepresentative {
    @Id
    private String id;
    private String SRId;
    private String SRName;
    private String userName;
    private String phoneNumber;
    private String email;
    private String territoryId;
    private String city;
    private String state;
    private UserStatus status;
    private LocalDateTime CreatedAt;
    private LocalDateTime UpdatedAt;





}
