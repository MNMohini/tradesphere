package com.bnagritech.tradesphere.order.dto;

import com.bnagritech.tradesphere.common.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderSearchRequest {
    private String orderNumber;
    private String outletId;
    private String promoterId;
    private String employeeId;
    private String territoryId;
    private OrderStatus orderStatus;
    private LocalDate fromDate;
    private LocalDate toDate;
    private Integer page=0;
    private Integer size=20;
    private String sortBy= "orderDate";
    private String sortDirection="DESC";

}
