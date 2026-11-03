package com.bnagritech.tradesphere.product.dto;

import com.bnagritech.tradesphere.common.enums.ProductStatus;
import com.bnagritech.tradesphere.common.enums.ProductUnit;
import com.bnagritech.tradesphere.product.model.PriceHistoryEntry;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** Full product details. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse {

    private String id;
    private String skuCode;
    private String name;
    private String description;
    private String brand;
    private String category;
    private ProductUnit unit;
    private String packSize;
    private String hsnCode;
    private String imageUrl;

    private BigDecimal mrp;
    private BigDecimal sellingPrice;
    private BigDecimal taxPercent;

    // Only filled for users who manage the catalogue
    private List<PriceHistoryEntry> priceHistory;

    private ProductStatus status;
    private String statusReason;

    private Instant createdAt;
    private Instant updatedAt;
}
