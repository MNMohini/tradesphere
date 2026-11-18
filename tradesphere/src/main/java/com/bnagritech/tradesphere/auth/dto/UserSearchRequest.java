package com.bnagritech.tradesphere.auth.dto;

import com.bnagritech.tradesphere.common.enums.UserRole;
import com.bnagritech.tradesphere.common.enums.UserStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Query parameters for GET /api/users, for example:
 * /api/users?keyword=ravi&role=SALES_REPRESENTATIVE&status=ACTIVE&limit=50
 * Results are always limited to the caller and the people below them.
 * There is no paging; "limit" (default 100, maximum 500) keeps one call from returning everything.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserSearchRequest {

    // Matched against username, full name, employee id, phone and email
    private String keyword;
    private UserRole role;
    private UserStatus status;
    private String managerId;
    private String territoryId;

    @Min(1)
    @Max(500)
    private Integer limit;
}
