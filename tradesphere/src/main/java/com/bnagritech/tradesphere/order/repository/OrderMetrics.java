package com.bnagritech.tradesphere.order.repository;

import java.math.BigDecimal;

public record OrderMetrics(
        String creatorReference,
        long totalOrders,
        BigDecimal orderValue,
        BigDecimal pendingAmount
) {
    public static OrderMetrics empty() {
        return new OrderMetrics(null, 0, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    public OrderMetrics plus(OrderMetrics other) {
        return new OrderMetrics(
                creatorReference,
                totalOrders + other.totalOrders,
                orderValue.add(other.orderValue),
                pendingAmount.add(other.pendingAmount));
    }
}
