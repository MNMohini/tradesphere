package com.bnagritech.tradesphere.order.dto;

import com.bnagritech.tradesphere.common.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponse {
    private String id;
    private String orderNumber;

    private String retailerId;
    private String retailerName;

    private String distributorId;
    private String distributorName;

    private String promoterId;
    private String promoterName;

    private List<OrderItemResponse> items;

    private Integer totalQuantity;
    private BigDecimal totalAmount;

    private OrderStatus status;

    private String remarks;

    private LocalDateTime orderDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
