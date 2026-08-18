package com.bnagritech.tradesphere.order.model;

import com.bnagritech.tradesphere.common.enums.OrderSource;
import com.bnagritech.tradesphere.common.enums.OrderStatus;
import com.bnagritech.tradesphere.common.enums.OrderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private LocalDate orderDate;
    private OrderType orderType;
    private OrderSource orderSource;
    private String outletId;
    private String promoterId;
    private String territoryId;
    private String outletName;
    private String phoneNumber;
    private String address;
    private List<OrderItems> items;
    private BigDecimal grossAmount;
    private BigDecimal itemDiscount;
    private BigDecimal orderDiscount;
    private BigDecimal taxableAmount;
    private BigDecimal taxAmount;
    private BigDecimal roundOff;
    private BigDecimal netAmount;
    private OrderStatus orderStatus;
    private List<OrderStatusHistory> statusHistory;
    private PaymentInfo paymentInfo;
    private DeliveryInfo deliveryInfo;
    private String cancellationReason;
    private String cancelledBy;
    private LocalDateTime cancelledAt;
    private String remarks;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
