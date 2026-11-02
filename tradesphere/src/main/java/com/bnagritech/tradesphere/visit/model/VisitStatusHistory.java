package com.bnagritech.tradesphere.visit.model;

import com.bnagritech.tradesphere.common.enums.VisitStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VisitStatusHistory {

    private VisitStatus fromStatus;
    private VisitStatus toStatus;
    private String updatedBy;      // user id, or "system" for automatic changes
    private String remarks;
    private Instant updatedAt;
}