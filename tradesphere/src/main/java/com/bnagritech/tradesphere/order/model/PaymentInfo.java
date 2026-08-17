package com.bnagritech.tradesphere.order.model;

import com.bnagritech.tradesphere.common.enums.PaymentMode;
import com.bnagritech.tradesphere.common.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentInfo {
    private PaymentStatus paymentStatus;
    private PaymentMode paymentMode;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal pendingAmount;
    private String transactionReference;
    private LocalDateTime finalPaymentDate;

}
