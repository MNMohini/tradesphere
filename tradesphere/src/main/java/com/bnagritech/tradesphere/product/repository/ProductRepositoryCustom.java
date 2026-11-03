package com.bnagritech.tradesphere.product.repository;

import com.bnagritech.tradesphere.product.model.Product;

import java.util.List;

public interface ProductRepositoryCustom {

    /** Products matching the filter, sorted by name, at most filter.limit(). */
    List<Product> search(ProductFilter filter);

    /** Distinct categories of ACTIVE products, sorted. */
    List<String> findActiveCategories();

    /** Distinct brands of ACTIVE products, sorted. */
    List<String> findActiveBrands();
}
