package com.bnagritech.tradesphere.scheme.dto;

import com.bnagritech.tradesphere.common.enums.SchemeStatus;
import com.bnagritech.tradesphere.common.enums.SchemeType;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class SchemeResponse {

    private String id;

    private String schemeCode;

    private String schemeName;

    private String description;

    private SchemeType schemeType;

    private LocalDate startDate;

    private LocalDate endDate;

    private List<String> productIds;

    private SchemeStatus status;

    private Instant createdAt;

    private Instant updatedAt;

    private String createdBy;

    private String updatedBy;
}