package com.bnagritech.tradesphere.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** Outcome for ONE order in a sync batch, matched to the app by clientOrderId. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SyncOrderResult {

    public enum Status {
        CREATED,     // saved as a new order
        DUPLICATE,   // already received earlier; the app can mark it synced
        REJECTED     // not saved; see errorMessage
    }

    private String clientOrderId;
    private Status status;

    // Filled for CREATED and DUPLICATE
    private String orderId;
    private String orderNumber;
    private BigDecimal grandTotal;       // the server's total

    // True when the server's total differs from expectedGrandTotal sent by the app
    private boolean priceChanged;

    // Filled for REJECTED, e.g. "Outlet not found", "Product 1001 is inactive"
    private String errorMessage;
}