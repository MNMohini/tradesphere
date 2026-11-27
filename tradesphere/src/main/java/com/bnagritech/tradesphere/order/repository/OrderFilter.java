package com.bnagritech.tradesphere.order.repository;

import com.bnagritech.tradesphere.common.enums.OrderStatus;
import com.bnagritech.tradesphere.common.enums.PaymentStatus;

import java.time.Instant;
import java.util.Collection;

/**
 * Optional order filters. A null creatorReferences value is reserved for an
 * explicitly unrestricted administrator query; an empty collection matches no orders.
 */
public record OrderFilter(
        Collection<String> creatorReferences,
        OrderStatus status,
        PaymentStatus paymentStatus,
        String outletCode,
        String createdBy,
        String orderNumber,
        String visitId,
        Instant fromInclusive,
        Instant toExclusive
) {
}
