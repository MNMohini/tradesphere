package com.bnagritech.tradesphere.visit.dto;

import com.bnagritech.tradesphere.common.enums.VisitStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Query parameters for GET /api/visits, for example:
 * /api/visits?status=COMPLETED&fromDate=2026-11-01&toDate=2026-11-07
 * With no dates, the last 7 days are returned. Results are always limited to
 * the visits the caller is allowed to see.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VisitSearchRequest {

    private String userId;
    private String outletId;
    private String beatId;
    private VisitStatus status;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fromDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate toDate;
}