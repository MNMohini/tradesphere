package com.bnagritech.tradesphere.order.model;

import com.bnagritech.tradesphere.common.enums.OrderStatus;

import java.time.LocalDateTime;

public class OrderStatusHistory {
    private OrderStatus orderStatus;
    private String changedBy;
    private String remark;
    private LocalDateTime changeAt;

}
