package com.bnagritech.tradesphere.scheme.dto;

import com.bnagritech.tradesphere.common.enums.SchemeStatus;
import com.bnagritech.tradesphere.common.enums.SchemeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class SchemeRequest {

    @NotBlank(message = "Scheme code is required")
    private String schemeCode;

    @NotBlank(message = "Scheme name is required")
    private String schemeName;

    private String description;

    @NotNull(message = "Scheme type is required")
    private SchemeType schemeType;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    private List<String> productIds;

    @NotNull(message = "Scheme status is required")
    private SchemeStatus status;
}