package com.bnagritech.tradesphere.product.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/** One price setting. The newest entry matches the product's current prices. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PriceHistoryEntry {

    private BigDecimal mrp;
    private BigDecimal sellingPrice;
    private BigDecimal taxPercent;
    private String changedBy;
    private Instant changedAt;
    private String remarks;
}
