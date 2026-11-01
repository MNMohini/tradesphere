package com.bnagritech.tradesphere.order.dto;

import com.bnagritech.tradesphere.common.enums.OrderStatus;
import com.bnagritech.tradesphere.common.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Query parameters for GET /api/orders, for example:
 * /api/orders?status=SUBMITTED&fromDate=2026-11-01&toDate=2026-11-30
 * The service must always restrict results to the users the caller is
 * allowed to see (hierarchy rule); the filters below only narrow it further.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderSearchRequest {

    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private String outletId;
    private String createdBy;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fromDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate toDate;
}