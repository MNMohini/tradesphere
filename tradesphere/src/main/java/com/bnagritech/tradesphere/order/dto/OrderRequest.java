package com.bnagritech.tradesphere.order.dto;

import com.bnagritech.tradesphere.common.enums.*;
import com.bnagritech.tradesphere.order.model.OrderItems;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {

    private String outletId;
    private OrderType orderType;
    private OrderSource orderSource;
    private String promoterId;
    private List<OrderItems> items;
    private String remarks;

}
