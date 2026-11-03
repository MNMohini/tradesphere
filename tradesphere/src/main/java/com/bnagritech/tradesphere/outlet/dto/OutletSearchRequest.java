package com.bnagritech.tradesphere.outlet.dto;

import com.bnagritech.tradesphere.common.enums.OutletGrade;
import com.bnagritech.tradesphere.common.enums.OutletStatus;
import com.bnagritech.tradesphere.common.enums.OutletType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Query parameters for GET /api/outlets, for example:
 * /api/outlets?keyword=sharma&city=Delhi&status=ACTIVE&limit=50
 * There is no paging; "limit" (default 100, maximum 500) keeps one call from returning everything.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OutletSearchRequest {

    // Matched against name, outlet code, owner name and phone number
    private String keyword;
    private OutletStatus status;
    private OutletType type;
    private OutletGrade grade;
    private String beatId;
    private String territoryId;
    private String city;
    private String pinCode;

    @Min(1)
    @Max(500)
    private Integer limit;
}
