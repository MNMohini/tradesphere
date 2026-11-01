package com.bnagritech.tradesphere.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * "What will this basket cost?" Nothing is saved.
 * The outlet is needed because prices and schemes can depend on it.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderPreviewRequest {

    @NotBlank(message = "outletId is required")
    private String outletId;

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<OrderItemRequest> items;
}
