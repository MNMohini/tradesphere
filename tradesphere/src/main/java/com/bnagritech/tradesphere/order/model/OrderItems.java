package com.bnagritech.tradesphere.order.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderItems {

    private String productId;
    private String productName;
    private String skuCode;
    private Integer quantity;
    private BigDecimal mrp;
    private BigDecimal sellingPrice;
    private BigDecimal discount;
    private BigDecimal taxableAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
}
