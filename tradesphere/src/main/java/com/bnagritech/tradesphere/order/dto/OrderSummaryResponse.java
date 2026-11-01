package com.bnagritech.tradesphere.order.dto;

import com.bnagritech.tradesphere.common.enums.DeliveryStatus;
import com.bnagritech.tradesphere.common.enums.OrderStatus;
import com.bnagritech.tradesphere.common.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/** Lightweight row for order lists (no items, no history). */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderSummaryResponse {

    private String id;
    private String orderNumber;
    private String outletId;
    private String outletName;
    private String createdBy;
    private String createdByName;
    private Instant orderDate;
    private int itemCount;
    private BigDecimal grandTotal;
    private BigDecimal pendingAmount;
    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private DeliveryStatus deliveryStatus;
}