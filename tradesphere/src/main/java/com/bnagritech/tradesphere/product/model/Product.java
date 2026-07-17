package com.bnagritech.tradesphere.product.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "products")
public class Product {
    private int id;
    private String productName;
    private String productCode;
    private String unitPerBox;
    private String MRP;
    private String PTR;
}
