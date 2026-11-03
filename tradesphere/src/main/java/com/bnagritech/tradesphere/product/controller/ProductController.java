package com.bnagritech.tradesphere.product.controller;

import com.bnagritech.tradesphere.auth.model.User;
import com.bnagritech.tradesphere.product.dto.CreateProductRequest;
import com.bnagritech.tradesphere.product.dto.ProductResponse;
import com.bnagritech.tradesphere.product.dto.ProductSearchRequest;
import com.bnagritech.tradesphere.product.dto.ProductSummaryResponse;
import com.bnagritech.tradesphere.product.dto.UpdateProductPriceRequest;
import com.bnagritech.tradesphere.product.dto.UpdateProductRequest;
import com.bnagritech.tradesphere.product.dto.UpdateProductStatusRequest;
import com.bnagritech.tradesphere.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST endpoints of the product module. The controller only receives the request,
 * validates it and hands it to ProductService; the rules live in the service.
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // POST /api/products
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody CreateProductRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(request, currentUser));
    }

    // GET /api/products?keyword=urea&category=Fertilizer&limit=50
    // GET /api/products?updatedAfter=2026-11-01T00:00:00Z     (catalogue sync for the app)
    @GetMapping
    public ResponseEntity<List<ProductSummaryResponse>> searchProducts(
            @Valid @ModelAttribute ProductSearchRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(productService.searchProducts(request, currentUser));
    }

    // GET /api/products/categories
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(productService.getCategories(currentUser));
    }

    // GET /api/products/brands
    @GetMapping("/brands")
    public ResponseEntity<List<String>> getBrands(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(productService.getBrands(currentUser));
    }

    // GET /api/products/sku/{skuCode}
    @GetMapping("/sku/{skuCode}")
    public ResponseEntity<ProductResponse> getProductBySku(
            @PathVariable String skuCode,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(productService.getProductBySku(skuCode, currentUser));
    }

    // GET /api/products/{productId}
    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProductById(
            @PathVariable String productId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(productService.getProductById(productId, currentUser));
    }

    // PUT /api/products/{productId}
    @PutMapping("/{productId}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable String productId,
            @Valid @RequestBody UpdateProductRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(productService.updateProduct(productId, request, currentUser));
    }

    // PATCH /api/products/{productId}/price
    @PatchMapping("/{productId}/price")
    public ResponseEntity<ProductResponse> updateProductPrice(
            @PathVariable String productId,
            @Valid @RequestBody UpdateProductPriceRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(productService.updateProductPrice(productId, request, currentUser));
    }

    // PATCH /api/products/{productId}/status
    @PatchMapping("/{productId}/status")
    public ResponseEntity<ProductResponse> updateProductStatus(
            @PathVariable String productId,
            @Valid @RequestBody UpdateProductStatusRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(productService.updateProductStatus(productId, request, currentUser));
    }
}
