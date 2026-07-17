package com.bnagritech.tradesphere.product.repository;

import com.bnagritech.tradesphere.product.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ProductRepository extends MongoRepository<Product, String> {
    Optional<Product> findProductByProductId(String productId);
    Optional<Product> findProductByProductName(String productName);
    Optional<Product> findProductBySkuCode(String skuCode);

    boolean existsProductBySkuCode(String skuCode);
    boolean existsProductByProductName(String productName);
    boolean existsProductByProductId(String productId);
}
