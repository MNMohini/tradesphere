package com.bnagritech.tradesphere.order.dto;
import com.bnagritech.tradesphere.common.enums.DeliveryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryInfoResponse {
    private String deliveryAddress;
    private DeliveryStatus deliveryStatus;
    private String deliveredBy;
    private LocalDateTime deliveredAt;
    private String deliveryRemark;
}
