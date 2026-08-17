package com.bnagritech.tradesphere.order.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
@Data
@Builder
public class OrderItemResponse {
    private String productId;
    private String productName;
    private BigDecimal mrp;
    private Integer quantity;
    private BigDecimal sellingPrice;
    private BigDecimal totalPrice;
}
