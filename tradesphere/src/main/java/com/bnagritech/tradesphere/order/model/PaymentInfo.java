package com.bnagritech.tradesphere.order.model;

import com.bnagritech.tradesphere.common.enums.PaymentMode;
import com.bnagritech.tradesphere.common.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentInfo {
    private PaymentStatus paymentStatus;
    private PaymentMode paymentMode;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal pendingAmount;
    private String transactionReference;
    private LocalDateTime finalPaymentDate;

}
