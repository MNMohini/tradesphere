package com.bnagritech.tradesphere.order.model;

import com.bnagritech.tradesphere.common.enums.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "orders")
public class Order {
    @Id
    private String id;
    private String orderNumber;
    private String outletId;
    private String promoterId;
    private String employeeId;
    private String territoryId;
    private LocalDateTime orderDate;
    private OrderStatus status;
    private List<OrderItems> items = new ArrayList<>();
    private BigDecimal subtotal;
    private BigDecimal totalDiscount;
    private BigDecimal totalSchemeDiscount;
    private BigDecimal totalTax;
    private BigDecimal grandTotal;
    private PaymentStatus paymentStatus;
    private PaymentMode paymentMethod;
    private BigDecimal paidAmount;
    private BigDecimal pendingAmount;
    private String paymentReference;
    private DeliveryStatus deliveryStatus;
    private String deliveryAddress;
    private LocalDateTime dispatchDate;
    private LocalDateTime deliveryDate;
    private String cancellationReason;
    private LocalDateTime cancelledAt;
    private String remarks;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderStatusHistory> statusHistory = new ArrayList<>();
}
