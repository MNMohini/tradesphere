package com.bnagritech.tradesphere.product.dto;

import com.bnagritech.tradesphere.common.enums.ProductStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;
    /**
     * Query parameters for GET /api/products, for example:
     * /api/products?keyword=urea&category=Fertilizer&limit=50
     * Field users see only ACTIVE products, EXCEPT when updatedAfter is sent. Then they also receive
     * products that were deactivated after that moment, so the app can hide them.
     * Example for the app's catalogue sync: /api/products?updatedAfter=2026-11-01T00:00:00Z
     * There is no paging; "limit" (default 100, maximum 500) keeps one call from returning everything.
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public class ProductSearchRequest {
        // Matched against name, SKU code and brand
        private String keyword;
        private String category;
        private String brand;
        private ProductStatus status;

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        private Instant updatedAfter;

        @Min(1)
        @Max(500)
        private Integer limit;

}
