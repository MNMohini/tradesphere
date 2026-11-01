package com.bnagritech.tradesphere.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One line the rep wants to order. Price, discount and tax are NOT accepted
 * from the client; the server looks them up and calculates them.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemRequest {

    @NotBlank(message = "productId is required")
    private String productId;
    @NotNull(message = "quantity is required")
    @Min(value = 1, message = "quantity must be at least 1")
    private Integer quantity;
    // Optional: scheme the rep selected (server still verifies eligibility)
    private String schemeId;
}