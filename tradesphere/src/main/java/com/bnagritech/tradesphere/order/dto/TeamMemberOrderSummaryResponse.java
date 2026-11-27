package com.bnagritech.tradesphere.order.dto;

import com.bnagritech.tradesphere.common.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeamMemberOrderSummaryResponse {

    private String id;
    private String userName;
    private String fullName;
    private UserRole role;
    private OrderTotalsResponse totals;
}
