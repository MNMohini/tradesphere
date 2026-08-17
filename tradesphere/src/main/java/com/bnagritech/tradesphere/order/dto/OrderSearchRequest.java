package com.bnagritech.tradesphere.order.dto;

import com.bnagritech.tradesphere.common.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderSearchRequest {
    private String orderNumber;
    private String outletId;
    private String promoterId;
    private String territoryId;
    private OrderStatus orderStatus;
    private LocalDate fromDate;
     private LocalDate toDate;
}
