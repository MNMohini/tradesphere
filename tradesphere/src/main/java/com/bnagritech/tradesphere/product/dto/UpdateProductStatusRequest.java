package com.bnagritech.tradesphere.product.dto;

import com.bnagritech.tradesphere.common.enums.ProductStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Activates or deactivates a product. A reason is required when deactivating. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProductStatusRequest {

    @NotNull(message = "status is required")
    private ProductStatus status;

    @Size(max = 500)
    private String remarks;
}
