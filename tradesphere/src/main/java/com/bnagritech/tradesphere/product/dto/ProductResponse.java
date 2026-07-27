package com.bnagritech.tradesphere.product.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductResponse {
    private String id;
    private String productId;
    private String productName;
    private String skuCode;
    private String MRP;
    private String PTR;
    private String imageUrl;
}
