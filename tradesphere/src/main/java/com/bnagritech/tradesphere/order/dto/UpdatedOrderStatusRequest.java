package com.bnagritech.tradesphere.order.dto;

import com.bnagritech.tradesphere.common.enums.OrderStatus;
import lombok.Data;

@Data
public class UpdatedOrderStatusRequest {
     private OrderStatus orderStatus;
     private String remarks;
}
