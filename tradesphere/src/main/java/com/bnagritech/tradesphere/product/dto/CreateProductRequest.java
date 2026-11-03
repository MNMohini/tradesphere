package com.bnagritech.tradesphere.product.dto;

import com.bnagritech.tradesphere.common.enums.ProductUnit;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** Creates a product. Not accepted from the client: id, status, price history, timestamps. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateProductRequest {

    @NotBlank(message = "skuCode is required")
    @Pattern(regexp = "^[A-Za-z0-9][A-Za-z0-9_./-]{1,39}$",
            message = "skuCode must be 2 to 40 characters: letters, digits, _ . / -")
    private String skuCode;

    @NotBlank(message = "name is required")
    @Size(max = 150)
    private String name;

    @Size(max = 1000)
    private String description;

    @Size(max = 100)
    private String brand;

    @NotBlank(message = "category is required")
    @Size(max = 100)
    private String category;

    @NotNull(message = "unit is required")
    private ProductUnit unit;

    @Size(max = 50)
    private String packSize;

    @Pattern(regexp = "^[0-9]{4,8}$", message = "HSN_Code must be 4 to 8 digits")
    private String hsnCode;

    @Size(max = 500)
    private String imageUrl;

    @NotNull(message = "mrp is required")
    @DecimalMin(value = "0.01", message = "mrp must be greater than zero")
    private BigDecimal mrp;

    // Price to the outlet before tax. Must not be above the MRP.
    @NotNull(message = "sellingPrice is required")
    @DecimalMin(value = "0.01", message = "sellingPrice must be greater than zero")
    private BigDecimal sellingPrice;

    @NotNull(message = "taxPercent is required")
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    private BigDecimal taxPercent;
}
