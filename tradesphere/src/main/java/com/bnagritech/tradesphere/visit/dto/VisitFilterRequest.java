package com.bnagritech.tradesphere.visit.dto;

import com.bnagritech.tradesphere.common.enums.VisitStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VisitFilterRequest {
    private String employeeId;
    private String retailerId;
    private String beatId;
    private String territoryId;
    private VisitStatus visitStatus;
    private LocalDate fromDate;
    private LocalDate toDate;
}
