package com.bnagritech.tradesphere.product.repository;

import com.bnagritech.tradesphere.common.enums.ProductStatus;

import java.time.Instant;

/**
 * Parameters for searching products. Every field except activeOnly and limit is optional.
 *
 * keyword:      matched, ignoring case, against name, SKU code and brand.
 * activeOnly:   true = only ACTIVE products (what field users normally get).
 * updatedAfter: only products changed after this moment (used by the app to download just the changes).
 */
public record ProductFilter(
        String keyword,
        String category,
        String brand,
        ProductStatus status,
        boolean activeOnly,
        Instant updatedAfter,
        int limit
) {
}
