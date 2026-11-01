package com.bnagritech.tradesphere.order.repository;

import com.bnagritech.tradesphere.common.enums.OrderStatus;
import com.bnagritech.tradesphere.common.enums.PaymentStatus;

import java.time.Instant;

/**
 * Parameters for searching orders. Every field except viewerUserId is optional.
 *
 * viewerUserId: the user who is asking. The result is limited to orders this user
 *               created or that were created by anyone below them in the hierarchy.
 *               Pass null ONLY for users who may see everything (ADMIN, NATIONAL_HEAD).
 * fromInclusive / toExclusive: order date range. The service converts the calendar
 *               dates from the request into these instants using IST (Asia/Kolkata).
 */
public record OrderFilter(
        String viewerUserId,
        OrderStatus status,
        PaymentStatus paymentStatus,
        String outletId,
        String createdBy,
        Instant fromInclusive,
        Instant toExclusive
) {
}