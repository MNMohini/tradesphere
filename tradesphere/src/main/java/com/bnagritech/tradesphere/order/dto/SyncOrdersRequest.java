package com.bnagritech.tradesphere.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

    /**
     * A batch of orders the app took while offline, uploaded when the network returns.
     * Each order is processed on its own, so one bad order never blocks the others.
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public class SyncOrdersRequest {

        private String deviceId;

        @NotEmpty(message = "orders list cannot be empty")
        @Size(max = 100, message = "Send at most 100 orders per sync call")
        @Valid
        private List<CreateOrderRequest> orders;
    }


