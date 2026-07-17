package com.bnagritech.tradesphere.product.service.impl;
import com.bnagritech.tradesphere.common.exception.ResourceNotFoundException;
import com.bnagritech.tradesphere.product.dto.ProductRequest;
import com.bnagritech.tradesphere.product.dto.ProductResponse;
import com.bnagritech.tradesphere.product.model.Product;
import com.bnagritech.tradesphere.product.repository.ProductRepository;
import com.bnagritech.tradesphere.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;

    @Override
    public ProductResponse addProduct(ProductRequest productRequest) {
        if(productRepository.existsProductByProductId(productRequest.getProductId()))
            throw new RuntimeException(
                    "Product with id " + productRequest.getProductId() + " already Exists");
        if (productRepository.existsProductByProductName(productRequest.getProductName()))
            throw new RuntimeException(
                    "Product with name " + productRequest.getProductName() + " already exists");
        if (productRepository.existsProductBySkuCode(productRequest.getSkuCode()))
            throw new RuntimeException(
                    "Product with sku code " + productRequest.getSkuCode() + " already exists");
        Product product = Product.builder()
                .id(productRequest.getId())
                .productId(productRequest.getProductId())
                .productName(productRequest.getProductName())
                .skuCode(productRequest.getSkuCode())
                .MRP(productRequest.getMRP())
                .PTR(productRequest.getPTR())
                .imageUrl(productRequest.getImageUrl())
                .build();

        Product addedProduct = productRepository.save(product);
        return mapToResponse(addedProduct);
    }

    @Override
    public ProductResponse updateProduct(String skuCode,ProductRequest productRequest) {
        Product product= productRepository.findProductBySkuCode(skuCode).
                orElseThrow(()->new ResourceNotFoundException
                        ("Product with id " + skuCode + " not found"));
        product.setProductName(productRequest.getProductName());
        product.setSkuCode(productRequest.getSkuCode());
        product.setProductId(productRequest.getProductId());
        product.setMRP(productRequest.getMRP());
        product.setPTR(productRequest.getPTR());
        product.setImageUrl(productRequest.getImageUrl());
       Product updatedProduct = productRepository.save(product);

        return mapToResponse(updatedProduct);
    }

    @Override
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ProductResponse getProductByProductId(String productId) {
       Product product = productRepository.findProductByProductId(productId)
               .orElseThrow(()->new ResourceNotFoundException(
                       "Product with id " + productId + " not found"));
        return mapToResponse(product);
    }

    @Override
    public ProductResponse getProductByProductName(String productName) {
        Product product = productRepository.findProductByProductName(productName)
                .orElseThrow(()->new ResourceNotFoundException(
                        "Product with id " + productName + " not found"));
        return mapToResponse(product);
    }

    @Override
    public ProductResponse getProductByProductSkuCode(String skuCode) {
        Product product = productRepository.findProductBySkuCode(skuCode)
                .orElseThrow(()->new ResourceNotFoundException(
                        "Product with id " + skuCode + " not found"));
        return mapToResponse(product);
    }
     public ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
            .id(product.getId())
            .productName(product.getProductName())
            .skuCode(product.getSkuCode())
            .MRP(product.getMRP())
            .PTR(product.getPTR())
            .imageUrl(product.getImageUrl())
            .build() ;
    }
}
