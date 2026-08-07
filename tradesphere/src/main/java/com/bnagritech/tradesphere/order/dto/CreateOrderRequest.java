package com.bnagritech.tradesphere.order.dto;

import com.bnagritech.tradesphere.common.enums.OrderStatus;
import com.bnagritech.tradesphere.common.enums.PaymentMode;
import com.bnagritech.tradesphere.common.enums.PaymentStatus;
import com.bnagritech.tradesphere.order.model.OrderItems;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderRequest {

    private String retailerId;
    private String promoterId;
    private String beatId;
    private String visitId;

    private List<OrderItems> items;

    private Integer totalQuantity;

    private OrderStatus status;
    private PaymentMode paymentMode;
    private PaymentStatus paymentStatus;

    private String remarks;

    private LocalDate orderDate;
    private LocalDate expectedDeliveryDate;

    private String createdBy = promoterId;
    private String updatedBy = promoterId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
