package com.bnagritech.tradesphere.visit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VisitCheckOutResponse {
    private String employeeId;
    private String beatId;
    private String territoryId;
    private String pictureUrl;
    private Double longitude;
    private Double latitude;
    private LocalDateTime checkInDate;
    private LocalDateTime checkOutDate;
    private String visitId;
}
