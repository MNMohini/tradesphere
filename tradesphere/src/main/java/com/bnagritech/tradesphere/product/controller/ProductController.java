package com.bnagritech.tradesphere.product.controller;
import com.bnagritech.tradesphere.product.dto.ProductRequest;
import com.bnagritech.tradesphere.product.dto.ProductResponse;
import com.bnagritech.tradesphere.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @PostMapping("/create")
    public ResponseEntity<ProductResponse> addProduct(@RequestBody ProductRequest request){
        return ResponseEntity.ok(productService.addProduct(request));
    }
    @GetMapping("/all")
    public ResponseEntity<List<ProductResponse>>getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }
    @PutMapping("/Id/{productId}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable String productId,
            @RequestBody ProductRequest request)
    {
        return ResponseEntity.ok(productService.updateProduct(productId,request));
    }
    @GetMapping("/Id/{productId}")
    public ResponseEntity<ProductResponse> getProductByProductId(@PathVariable String productId){
        return ResponseEntity.ok(productService.getProductByProductId(productId));
    }
    @GetMapping("/name/{productName}")
    public ResponseEntity<ProductResponse> getProductByProductName(@PathVariable String productName){
        return ResponseEntity.ok(productService.getProductByProductName(productName));
    }
    @GetMapping("/Id/{skuCode}")
    public ResponseEntity<ProductResponse> getProductBySkuCode(@PathVariable String skuCode){
        return ResponseEntity.ok(productService.getProductByProductSkuCode(skuCode));
    }
}
