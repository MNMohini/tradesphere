package com.bnagritech.tradesphere.visit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VisitCheckInRequest {
    private String checkinDate;
    private String checkoutDate;
    private  String visitId;
    private String employeeId;
    private String beatId;
    private String TerritoryId;
    private String pictureUrl;
    private Double longitude;
    private Double latitude;
}
