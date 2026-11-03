package com.bnagritech.tradesphere.product.repository;

import com.bnagritech.tradesphere.product.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ProductRepository extends MongoRepository<Product, String>, ProductRepositoryCustom {

    Optional<Product> findBySkuCode(String skuCode);

    /**
     * SKU codes must not repeat.
     */
    boolean existsBySkuCode(String skuCode);
}