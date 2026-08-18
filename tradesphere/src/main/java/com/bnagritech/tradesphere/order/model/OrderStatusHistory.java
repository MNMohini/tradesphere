package com.bnagritech.tradesphere.order.model;

import com.bnagritech.tradesphere.common.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderStatusHistory {
    private OrderStatus orderStatus;
    private String updatedBy;
    private String remarks;
    private LocalDateTime updatedAt;

}
