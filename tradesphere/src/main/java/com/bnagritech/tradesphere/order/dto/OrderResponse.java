package com.bnagritech.tradesphere.order.dto;

import com.bnagritech.tradesphere.common.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private String id;
    private String orderNumber;
    private String outletId;
    private String promoterId;
    private String employeeId;
    private String territoryId;
    private List<OrderItemResponse> items;
    private BigDecimal subtotal;
    private BigDecimal totalDiscount;
    private BigDecimal totalSchemeDiscount;
    private BigDecimal totalTax;
    private BigDecimal grandTotal;
    private PaymentInfoResponse payment;
    private DeliveryInfoResponse delivery;
    public String cancellationReason;
    private LocalDateTime cancelledAt;
    private OrderStatus status;
    private String remarks;
    private String createdBy;
    private String updatedBy;
    private List<OrderStatusHistoryResponse> statusHistory;
    private LocalDateTime orderDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
