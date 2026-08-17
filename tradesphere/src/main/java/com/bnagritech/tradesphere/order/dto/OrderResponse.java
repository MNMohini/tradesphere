package com.bnagritech.tradesphere.order.dto;

import com.bnagritech.tradesphere.common.enums.OrderSource;
import com.bnagritech.tradesphere.common.enums.OrderStatus;
import com.bnagritech.tradesphere.common.enums.OrderType;
import com.bnagritech.tradesphere.order.model.DeliveryInfo;
import com.bnagritech.tradesphere.order.model.OrderItems;
import com.bnagritech.tradesphere.order.model.OrderStatusHistory;
import com.bnagritech.tradesphere.order.model.PaymentInfo;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponse {
    @Id
    private String id;
    private String orderNumber;
    private LocalDateTime orderDate;
    private OrderType orderType;
    private OrderSource orderSource;
    private String outletId;
    private String promoterId;
    private String employeeId;
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
