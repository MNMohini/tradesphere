package com.bnagritech.tradesphere.product.dto;

import com.bnagritech.tradesphere.common.enums.ProductStatus;
import com.bnagritech.tradesphere.common.enums.ProductUnit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/** One row in a product list or in the app's catalogue. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductSummaryResponse {

    private String id;
    private String skuCode;
    private String name;
    private String brand;
    private String category;
    private ProductUnit unit;
    private String packSize;
    private BigDecimal mrp;
    private BigDecimal sellingPrice;
    private BigDecimal taxPercent;
    private String imageUrl;
    private ProductStatus status;
    private Instant updatedAt;
}
