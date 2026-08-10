package com.bnagritech.tradesphere.product.service;

import com.bnagritech.tradesphere.product.dto.ProductRequest;
import com.bnagritech.tradesphere.product.dto.ProductResponse;
import java.util.List;


public interface ProductService {
        ProductResponse addProduct(ProductRequest productRequest);
        ProductResponse updateProduct(String skuCode,ProductRequest productRequest);
        List<ProductResponse> getAllProducts();
        ProductResponse getProductByProductId(String productId);
        ProductResponse getProductByProductName(String productName);
        ProductResponse getProductByProductSkuCode(String skuCode);
        void deleteProduct(String productId);

}
