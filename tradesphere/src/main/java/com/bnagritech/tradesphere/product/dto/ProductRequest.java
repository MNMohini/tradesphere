package com.bnagritech.tradesphere.product.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProductRequest {
    private String id;
    @NotBlank(message = "Product name is required")
    private String productName;
    @NotBlank(message = "Product Id is required")
    private String productId;
    @NotBlank(message = "SKUCode is required")
    private String skuCode;
    @NotBlank(message = "MRP is required")
    private String MRP;
    @NotBlank(message = "PTR is required")
    private String PTR;
    @NotBlank(message = "Image is required")
    private String imageUrl;
}
