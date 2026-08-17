package com.bnagritech.tradesphere.order.model;

import com.bnagritech.tradesphere.common.enums.DeliveryStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DeliveryInfo {
    private String deliveryAddress;
    private DeliveryStatus deliveryStatus;
    private LocalDate expectedDeliveryDate;
    private LocalDate actualDeliveryDate;
    private String deliveredBy;
    private LocalDateTime deliveredAt;
    private String deliveryRemark;

}
