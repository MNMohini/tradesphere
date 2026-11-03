package com.bnagritech.tradesphere.visit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CancelVisitRequest {

    @NotBlank(message = "reason is required")
    @Size(max = 500)
    private String reason;
}