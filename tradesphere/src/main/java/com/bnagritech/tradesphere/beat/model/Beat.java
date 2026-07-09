package com.bnagritech.tradesphere.beat.model;

import com.bnagritech.tradesphere.common.enums.BeatStatus;
import com.bnagritech.tradesphere.common.enums.RetailerType;
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
@Document(collection = "beats")
public class Beat {
    @Id
    private String id;
    private String beatId;
    private String beatCode;
    private String beatName;
    private String description;

    private String territoryId;

    private String promoterId;

    private RetailerType BeatType;

    private List<String> retailerId;
    private Integer retailerCount;

    private String assignedEmployeeID;
    private String employeeName;
    private String employeePhone;
    private String employeeEmail;
    private List<String> visitDay;

    private BeatStatus active;
    private Boolean isDeleted;
    private LocalDateTime updatedAt;
    private LocalDateTime createdAt;
    private String createdBy;
    private String updatedBy;

    private String country;
    private String state;
    private String city;
    private String pinCode;
    private String area;
    private Double latitude;
    private Double longitude;
}
