package com.bnagritech.tradesphere.order.dto;

import com.bnagritech.tradesphere.common.enums.PaymentMode;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Records a payment against an order. The amount is ADDED to the amount
 * already paid; the service must reject payments larger than the pending amount.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecordPaymentRequest {

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", message = "amount must be greater than zero")
    private BigDecimal amount;

    @NotNull(message = "paymentMode is required")
    private PaymentMode paymentMode;

    @Size(max = 100)
    private String transactionReference;
}