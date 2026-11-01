package com.bnagritech.tradesphere.order.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * One line of an order. Embedded inside Order (not a separate collection).
 * Name, price and tax are snapshots taken at order time.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderItems {

    private String productId;
    private String skuCode;
    private String productName;
    private String unit;                 // PCS, CASE, KG ...
    private Integer quantity;

    private BigDecimal mrp;
    private BigDecimal sellingPrice;
    private BigDecimal discount;
    private BigDecimal taxableAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;      // final line total

    private String schemeId;
}