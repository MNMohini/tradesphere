package com.bnagritech.tradesphere.product.service;

import com.bnagritech.tradesphere.auth.model.User;
import com.bnagritech.tradesphere.product.dto.CreateProductRequest;
import com.bnagritech.tradesphere.product.dto.ProductResponse;
import com.bnagritech.tradesphere.product.dto.ProductSearchRequest;
import com.bnagritech.tradesphere.product.dto.ProductSummaryResponse;
import com.bnagritech.tradesphere.product.dto.UpdateProductPriceRequest;
import com.bnagritech.tradesphere.product.dto.UpdateProductRequest;
import com.bnagritech.tradesphere.product.dto.UpdateProductStatusRequest;

import java.util.List;

/**
 * The product catalogue. Everyone logged in can read ACTIVE products; only catalogue managers
 * (ADMIN and NATIONAL_HEAD) can create, change prices or switch products on and off.
 */
public interface ProductService {

        /** Creates a product (ACTIVE) with its first price entry. */
        ProductResponse createProduct(CreateProductRequest request, User currentUser);

        /** Changes details such as name, brand or category. Prices have their own method. */
        ProductResponse updateProduct(String productId, UpdateProductRequest request, User currentUser);

        /** Sets new prices and tax, and records the change in the price history. */
        ProductResponse updateProductPrice(String productId, UpdateProductPriceRequest request, User currentUser);

        /** Activates or deactivates a product. */
        ProductResponse updateProductStatus(String productId, UpdateProductStatusRequest request, User currentUser);

        /** One product. Field users get a 404 for inactive products. */
        ProductResponse getProductById(String productId, User currentUser);

        /** One product found by SKU code. */
        ProductResponse getProductBySku(String skuCode, User currentUser);

        /** Products the caller may see, narrowed by the filters, sorted by name. */
        List<ProductSummaryResponse> searchProducts(ProductSearchRequest request, User currentUser);

        /** Categories of ACTIVE products. */
        List<String> getCategories(User currentUser);

        /** Brands of ACTIVE products. */
        List<String> getBrands(User currentUser);
}
