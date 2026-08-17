package com.bnagritech.tradesphere.order.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderItemRequest {
    private String skuCode;
    private BigDecimal sellingPrice;
    private Integer quantity;
}
