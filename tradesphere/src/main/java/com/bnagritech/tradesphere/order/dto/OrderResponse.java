package com.bnagritech.tradesphere.order.dto;

import com.bnagritech.tradesphere.common.enums.OrderStatus;
import com.bnagritech.tradesphere.order.model.DeliveryInfo;
import com.bnagritech.tradesphere.order.model.OrderStatusHistory;
import com.bnagritech.tradesphere.order.model.PaymentInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Full order details (order detail screen). PaymentInfo, DeliveryInfo and
 * OrderStatusHistory are plain value objects with nothing sensitive, so they are
 * reused here. Internal fields (version, userPath) are not exposed.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {

    private String id;
    private String orderNumber;
    private String clientOrderId;

    private String outletId;
    private String outletName;
    private String visitId;
    private String beatId;
    private String territoryId;

    private String createdBy;
    private String createdByName;

    private Instant orderDate;
    private LocationDto location;

    private List<OrderItemResponse> items;

    private BigDecimal subtotal;
    private BigDecimal totalDiscount;
    private BigDecimal totalSchemeDiscount;
    private BigDecimal totalTax;
    private BigDecimal grandTotal;
    private BigDecimal pendingAmount;

    private OrderStatus status;
    private String cancellationReason;
    private Instant cancelledAt;
    private String remarks;

    private PaymentInfo payment;
    private DeliveryInfo delivery;
    private List<OrderStatusHistory> statusHistory;

    private Instant createdAt;
    private Instant updatedAt;
}