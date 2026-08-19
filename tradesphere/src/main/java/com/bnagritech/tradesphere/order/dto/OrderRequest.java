package com.bnagritech.tradesphere.order.dto;

import com.bnagritech.tradesphere.common.enums.*;
import com.bnagritech.tradesphere.order.model.OrderItems;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {

    private String outletId;
    private String territoryId;
    private String employeeId;
    private String promoterId;
    private List<OrderItemRequest> items;
    private String remarks;
}
