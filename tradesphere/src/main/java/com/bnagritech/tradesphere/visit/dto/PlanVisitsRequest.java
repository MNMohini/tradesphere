package com.bnagritech.tradesphere.visit.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Plans one separate visit per outlet for one day.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlanVisitsRequest {

    @NotNull(message = "plannedDate is required")
    private LocalDate plannedDate;

    @NotEmpty(message = "At least one outlet is required")
    @Size(max = 20, message = "Plan at most 20 outlets at a time")
    private List<String> outletCodes;   // outlet code or outlet id; each creates a separate visit

    private String beatId;              // optional
}
