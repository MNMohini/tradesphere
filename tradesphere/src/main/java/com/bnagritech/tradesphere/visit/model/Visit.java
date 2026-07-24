package com.bnagritech.tradesphere.visit.model;

import com.bnagritech.tradesphere.common.VisitType;
import com.bnagritech.tradesphere.common.enums.VisitOutCome;
import com.bnagritech.tradesphere.common.enums.VisitStatus;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "visits")
public class Visit {
    @Id
    private String id;
    private String visitCode;
    private String employeeId;
    private String beatId;
    private String retailerId;
    private String cityId;
    private String cityName;
    private String countryId;
    private String countryName;
    private String territoryId;
    private LocalDateTime plannedDate;
    private LocalDateTime checkInDate;
    private LocalDateTime checkOutDate;
    private Double longitude;
    private Double latitude;
    private Double gpsAccuracy;
    private VisitStatus visitStatus;
    private VisitType visitType;
    private VisitOutCome visitOutCome;
    private String remarks;
    private Integer durationInMinutes;
    private Boolean orderBooked;
    private Boolean stockVerified;
    private Boolean merchandisingDone;
    private Boolean paymentCollected;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;


}
