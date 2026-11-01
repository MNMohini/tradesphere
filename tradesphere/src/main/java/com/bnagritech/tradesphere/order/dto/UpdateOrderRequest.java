package com.bnagritech.tradesphere.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Edits an order that is still in DRAFT. The items list REPLACES the existing
 * items, and the server recalculates every amount. The service must reject
 * the call if the order is no longer DRAFT or does not belong to the caller.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateOrderRequest {

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<OrderItemRequest> items;

    @Size(max = 500)
    private String deliveryAddress;

    private LocalDate expectedDeliveryDate;

    @Size(max = 500)
    private String remarks;
}
