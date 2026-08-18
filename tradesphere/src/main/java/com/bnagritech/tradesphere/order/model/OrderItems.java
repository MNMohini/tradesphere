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

    private String skuCode;
    private String productName;

    private Integer orderedQuantity;
    private Integer approvedQuantity;
    private Integer deliveredQuantity;

    private BigDecimal mrp;
    private BigDecimal unitPrice;

    private BigDecimal discountPercentage;
    private BigDecimal discountAmount;

    private BigDecimal taxPercentage;
    private BigDecimal taxAmount;

    private BigDecimal lineAmount;

    private String schemeId;
    private String schemeName;
}
