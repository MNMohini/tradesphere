package com.bnagritech.tradesphere.order.model;

import com.bnagritech.tradesphere.common.enums.DeliveryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryInfo {

    private String deliveryAddress;
    private DeliveryStatus deliveryStatus;

    // Calendar dates (no time zone needed)
    private LocalDate expectedDeliveryDate;
    private LocalDate actualDeliveryDate;

    private Instant dispatchedAt;
    private String deliveredBy;
    private Instant deliveredAt;
    private String deliveryRemark;
}