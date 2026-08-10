package com.bnagritech.tradesphere.visit.dto;
import com.bnagritech.tradesphere.common.enums.VisitType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
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
    private String visitCode;

    private String remarks;
}
