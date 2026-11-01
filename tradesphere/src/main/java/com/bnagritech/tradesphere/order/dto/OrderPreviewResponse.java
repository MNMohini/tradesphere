package com.bnagritech.tradesphere.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/** Server-calculated prices, discounts, schemes and totals for a basket. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderPreviewResponse {

    private List<OrderItemResponse> items;

    private BigDecimal subtotal;
    private BigDecimal totalDiscount;
    private BigDecimal totalSchemeDiscount;
    private BigDecimal totalTax;
    private BigDecimal grandTotal;

    // Messages for the rep, e.g. "Scheme S12 not applicable", "SKU 1001 is inactive"
    private List<String> warnings;
}

