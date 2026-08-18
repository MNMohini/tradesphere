package com.bnagritech.tradesphere.order.model;

import com.bnagritech.tradesphere.common.enums.OrderStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderStatusHistory {
    private OrderStatus orderStatus;
    private String updatedBy;
    private String remarks;
    private LocalDateTime updatedAt;

}
