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
 * Plans one visit (VI000000001) covering several outlets on one day.
 * Planning again for the same day adds the new outlets to that visit.
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
    private List<String> outletCodes;   // outlet code or outlet id

    private String beatId;              // optional
}
