package com.bnagritech.tradesphere.order.dto;

import com.bnagritech.tradesphere.common.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatedOrderStatusRequest {
     private OrderStatus orderStatus;
     private String remarks;
}
