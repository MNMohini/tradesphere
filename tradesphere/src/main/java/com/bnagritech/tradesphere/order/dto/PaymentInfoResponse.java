package com.bnagritech.tradesphere.order.dto;

import com.bnagritech.tradesphere.common.enums.PaymentMode;
import com.bnagritech.tradesphere.common.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PaymentInfoResponse {
    private PaymentStatus paymentStatus;
    private PaymentMode paymentMode;
    private BigDecimal paidAmount;
    private BigDecimal pendingAmount;
    private String transactionReference;

}
