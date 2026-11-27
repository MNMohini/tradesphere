package com.bnagritech.tradesphere.order.repository;

import com.bnagritech.tradesphere.common.enums.OrderStatus;
import com.bnagritech.tradesphere.common.enums.PaymentStatus;

import java.time.Instant;

/**
 * Parameters for searching orders. Every field except the viewer identities is optional.
 *
 * The viewer identities are matched against both ID- and username-based records.
 * Pass both as null ONLY for users who may see everything (ADMIN, NATIONAL_HEAD).
 * fromInclusive / toExclusive: order date range. The service converts the calendar
 *               dates from the request into these instants using IST (Asia/Kolkata).
 */
public record OrderFilter(
        String viewerUserId,
        String viewerUsername,
        OrderStatus status,
        PaymentStatus paymentStatus,
        String outletCode,
        String createdBy,
        Instant fromInclusive,
        Instant toExclusive
) {
}