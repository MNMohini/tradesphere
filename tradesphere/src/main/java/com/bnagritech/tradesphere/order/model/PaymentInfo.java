package com.bnagritech.tradesphere.order.model;

import com.bnagritech.tradesphere.common.enums.PaymentMode;
import com.bnagritech.tradesphere.common.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Payment details of an order. The total comes from Order.grandTotal and the
 * pending amount from Order.getPendingAmount(), so neither is stored here.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentInfo {

    private PaymentStatus paymentStatus;
    private PaymentMode paymentMode;
    private BigDecimal paidAmount;
    private String transactionReference;
    private Instant finalPaymentDate;
}