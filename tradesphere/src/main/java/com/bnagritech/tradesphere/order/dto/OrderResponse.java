package com.bnagritech.tradesphere.order.dto;

import com.bnagritech.tradesphere.common.enums.OrderSource;
import com.bnagritech.tradesphere.common.enums.OrderStatus;
import com.bnagritech.tradesphere.common.enums.OrderType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponse {

    private OrderType orderType;
    private OrderSource orderSource;
    private String outletId;
    private String promoterId;
    private String territoryId;
    private String remarks;
    private List<OrderItemRequest> items;

}
