package com.bnagritech.tradesphere.order.dto;

import com.bnagritech.tradesphere.common.enums.DeliveryStatus;
import com.bnagritech.tradesphere.common.enums.OrderStatus;
import com.bnagritech.tradesphere.common.enums.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderSummaryResponse {

    private String id;
    private String orderNumber;
    private String outletId;
    private String promoterId;
    private String employeeId;
    private String territoryId;
    private LocalDateTime orderDate;
    private OrderStatus orderStatus;
    private Integer totalItems;
    private BigDecimal grandTotal;
    private PaymentStatus paymentStatus;
    private DeliveryStatus deliveryStatus;

}
