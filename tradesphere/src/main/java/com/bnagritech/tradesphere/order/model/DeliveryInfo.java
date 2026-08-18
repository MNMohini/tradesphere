package com.bnagritech.tradesphere.order.model;

import com.bnagritech.tradesphere.common.enums.DeliveryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryInfo {
    private String deliveryAddress;
    private DeliveryStatus deliveryStatus;
    private LocalDate expectedDeliveryDate;
    private LocalDate actualDeliveryDate;
    private String deliveredBy;
    private LocalDateTime deliveredAt;
    private String deliveryRemark;

}
