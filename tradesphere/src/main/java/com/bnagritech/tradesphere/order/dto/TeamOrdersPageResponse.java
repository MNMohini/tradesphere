package com.bnagritech.tradesphere.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeamOrdersPageResponse {

    private List<OrderSummaryResponse> items;
    private OrderTotalsResponse totals;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
