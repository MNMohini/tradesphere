package com.bnagritech.tradesphere.product.service.impl;
import com.bnagritech.tradesphere.common.exception.ResourceAlreadyExistsException;
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
            throw new ResourceAlreadyExistsException(" already Exists");
        if (productRepository.existsProductByProductName(productRequest.getProductName()))
            throw new ResourceAlreadyExistsException(" already exists");
        if (productRepository.existsProductBySkuCode(productRequest.getSkuCode()))
            throw new ResourceAlreadyExistsException(" already exists");
        Product product = Product.builder()
                .id(productRequest.getId())
                .productId(productRequest.getProductId())
                .productName(productRequest.getProductName())
                .skuCode(productRequest.getSkuCode())
                .mrp(productRequest.getMrp())
                .ptr(productRequest.getPtr())
                .imageUrl(productRequest.getImageUrl())
                .build();

        Product addedProduct = productRepository.save(product);
        return mapToResponse(addedProduct);
    }

    @Override
    public ProductResponse updateProduct(String productId,ProductRequest productRequest) {
        Product product= productRepository.findProductByProductId(productRequest.getProductId()).
                orElseThrow(()->new ResourceNotFoundException
                        ("Product not found"));
        product.setProductName(productRequest.getProductName());
        product.setSkuCode(productRequest.getSkuCode());
        product.setMrp(productRequest.getMrp());
        product.setPtr(productRequest.getPtr());
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
                       "Product not found"));
        return mapToResponse(product);
    }

    @Override
    public ProductResponse getProductByProductName(String productName) {
        Product product = productRepository.findProductByProductName(productName)
                .orElseThrow(()->new ResourceNotFoundException(
                        "Product not found"));
        return mapToResponse(product);
    }

    @Override
    public ProductResponse getProductByProductSkuCode(String skuCode) {
        Product product = productRepository.findProductBySkuCode(skuCode)
                .orElseThrow(()->new ResourceNotFoundException(
                        "Product not found"));
        return mapToResponse(product);
    }

    @Override
    public void deleteProduct(String productId) {
            Product product= productRepository.findProductByProductId(productId)
                    .orElseThrow(()->new ResourceNotFoundException("product not found"));
            productRepository.delete(product);
    }


    public ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
            .id(product.getId())
            .productId(product.getProductId())
            .productName(product.getProductName())
            .skuCode(product.getSkuCode())
            .mrp(product.getMrp())
            .ptr(product.getPtr())
            .imageUrl(product.getImageUrl())
            .build() ;
    }
}
