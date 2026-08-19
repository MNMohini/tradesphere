package com.bnagritech.tradesphere.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponse {
    private String productId;
    private String productName;
    private String skuCode;
    private Integer quantity;
    private BigDecimal sellingPrice;
    private BigDecimal mrp;
    private BigDecimal discount;
    private BigDecimal schemeDiscount;
    private BigDecimal taxAmount;
    private BigDecimal lineTotal;
}
