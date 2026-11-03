package com.bnagritech.tradesphere.product.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** Sets new prices. Send all three values; the change is stored in the price history. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProductPriceRequest {

    @NotNull(message = "mrp is required")
    @DecimalMin(value = "0.01", message = "mrp must be greater than zero")
    private BigDecimal mrp;

    @NotNull(message = "sellingPrice is required")
    @DecimalMin(value = "0.01", message = "sellingPrice must be greater than zero")
    private BigDecimal sellingPrice;

    @NotNull(message = "taxPercent is required")
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    private BigDecimal taxPercent;

    // Why the price changed (for example "Revised rate list Nov 2026")
    @Size(max = 500)
    private String remarks;
}
