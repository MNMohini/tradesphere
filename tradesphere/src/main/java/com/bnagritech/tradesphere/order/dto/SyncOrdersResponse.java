package com.bnagritech.tradesphere.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SyncOrdersResponse {

    private int total;
    private int created;
    private int duplicates;
    private int rejected;

    private List<SyncOrderResult> results;

    private Instant serverTime;
}
