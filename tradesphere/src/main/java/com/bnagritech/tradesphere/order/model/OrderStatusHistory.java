package com.bnagritech.tradesphere.order.model;

import com.bnagritech.tradesphere.common.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderStatusHistory {

    private OrderStatus fromStatus;
    private OrderStatus toStatus;
    private String updatedBy;        // user id
    private String remarks;
    private Instant updatedAt;
}