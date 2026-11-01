package com.bnagritech.tradesphere.order.dto;

import com.bnagritech.tradesphere.common.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Used to approve, reject, cancel or complete an order.
 * The service must check that the transition is allowed and, when
 * the new status is CANCELLED or REJECTED, that a reason is given.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdatedOrderStatusRequest {

     @NotNull(message = "status is required")
     private OrderStatus status;

     @Size(max = 500)
     private String remarks;

     @Size(max = 500)
     private String cancellationReason;
}