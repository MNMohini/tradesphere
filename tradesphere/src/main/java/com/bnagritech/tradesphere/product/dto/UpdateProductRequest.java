package com.bnagritech.tradesphere.product.dto;

import com.bnagritech.tradesphere.common.enums.ProductUnit;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Changes product details. Only the fields that are sent (not null) are changed.
 * The SKU code cannot be changed. Prices and tax change through the price endpoint,
 * so every price change is recorded.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProductRequest {

    @Size(max = 150)
    private String name;

    @Size(max = 1000)
    private String description;

    @Size(max = 100)
    private String brand;

    @Size(max = 100)
    private String category;

    private ProductUnit unit;

    @Size(max = 50)
    private String packSize;

    @Pattern(regexp = "^[0-9]{4,8}$", message = "HSN_Code must be 4 to 8 digits")
    private String hsnCode;

    @Size(max = 500)
    private String imageUrl;
}
