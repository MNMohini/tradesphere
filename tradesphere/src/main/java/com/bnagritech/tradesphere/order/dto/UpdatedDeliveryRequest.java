package com.bnagritech.tradesphere.order.dto;

import com.bnagritech.tradesphere.common.enums.DeliveryStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdatedDeliveryRequest {

    @NotNull(message = "deliveryStatus is required")
    private DeliveryStatus deliveryStatus;

    private LocalDate expectedDeliveryDate;
    private LocalDate actualDeliveryDate;

    @Size(max = 100)
    private String deliveredBy;

    @Size(max = 500)
    private String deliveryRemark;
}