package com.bnagritech.tradesphere.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderSummaryResponse {
    private long totalOrders;
    private long confirmedOrder;
    private long createdOrders;
    private long processingOrders;
    private long dispatchedOrders;
    private long deliveredOrders;
    private long cancelledOrders;
    private double totalSales;

}
