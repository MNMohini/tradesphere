package com.bnagritech.tradesphere.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderTotalsResponse {

    private long totalOrders;
    private BigDecimal orderValue;
    private BigDecimal pendingAmount;
}
