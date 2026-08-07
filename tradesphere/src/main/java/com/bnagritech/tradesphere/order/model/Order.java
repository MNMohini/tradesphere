package com.bnagritech.tradesphere.order.model;

import com.bnagritech.tradesphere.common.enums.OrderStatus;
import com.bnagritech.tradesphere.common.enums.OrderType;
import com.bnagritech.tradesphere.common.enums.PaymentMode;
import com.bnagritech.tradesphere.common.enums.PaymentStatus;
import com.bnagritech.tradesphere.product.model.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "orders")
public class Order {
    private String id;
    private String orderNumber;

    private String employeeId;
    private String retailerId;
    private String promoterId;
    private String beatId;
    private String visitId;

    private List<OrderItems> items;

    private Integer totalQuantity;

    private BigDecimal grossAmount;
    private BigDecimal discountAmount;
    private BigDecimal schemeDiscount;
    private BigDecimal taxableAmount;
    private BigDecimal taxAmount;
    private BigDecimal netAmount;

    private OrderType orderType;
    private OrderStatus status;
    private PaymentMode paymentMode;
    private PaymentStatus paymentStatus;

    private String remarks;
    private String cancellationReason;

    private LocalDate orderDate;
    private LocalDate expectedDeliveryDate;

    private String createdBy;
    private String updatedBy;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
