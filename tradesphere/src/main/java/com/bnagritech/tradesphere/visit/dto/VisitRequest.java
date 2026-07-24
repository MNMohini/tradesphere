package com.bnagritech.tradesphere.visit.dto;

import com.bnagritech.tradesphere.common.VisitType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class VisitRequest {
    @NotBlank(message = "Employee Id is required")
    private String employeeId;

    @NotBlank(message = "Retailer Id is required")
    private String retailerId;

    @NotBlank(message = "Beat Id is required")
    private String beatId;

    @NotBlank(message = "Territory Id is required")
    private String territoryId;

    @NotNull(message = "Planned date is required")
    private LocalDateTime plannedDate;

    @NotNull(message = "Visit type is required")
    private VisitType visitType;

    private String remarks;
}
