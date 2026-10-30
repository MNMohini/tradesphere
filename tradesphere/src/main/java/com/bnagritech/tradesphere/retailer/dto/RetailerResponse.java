package com.bnagritech.tradesphere.retailer.dto;
import com.bnagritech.tradesphere.common.enums.RetailerStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetailerResponse {

    private String id;
    private String retailerId;
    private String retailerName;
    private List<String> outletIds;
    private RetailerStatus retailerStatus;
    private LocalDateTime createdAt;
}
