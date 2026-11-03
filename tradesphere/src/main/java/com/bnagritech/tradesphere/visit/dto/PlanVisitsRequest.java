package com.bnagritech.tradesphere.visit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/** Plans visits to several outlets on one day for the logged-in user. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlanVisitsRequest {

    @NotNull(message = "plannedDate is required")
    private LocalDate plannedDate;

    private String beatId;

    @NotEmpty(message = "At least one outlet is required")
    @Size(max = 200, message = "Plan at most 200 outlets at a time")
    private List<@NotBlank String> outletIds;
}