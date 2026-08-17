package com.bnagritech.tradesphere.order.dto;

import com.bnagritech.tradesphere.common.enums.OrderStatus;

import java.time.LocalDateTime;

public class OrderStatusHistoryResponse {
    private OrderStatus orderStatus;
    private String updatedBy;
    private String remarks;
    private LocalDateTime updatedAt;

}
