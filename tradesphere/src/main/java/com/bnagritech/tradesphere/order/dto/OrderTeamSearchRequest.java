package com.bnagritech.tradesphere.order.dto;

import com.bnagritech.tradesphere.common.enums.OrderStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder.Default;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderTeamSearchRequest {

    private String memberId;
    private boolean myOrdersOnly;
    @Default
    private boolean includeTeam = true;
    private OrderStatus status;

    @Size(max = 100)
    private String orderNumber;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fromDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate toDate;

    @Min(0)
    @Default
    private int page = 0;

    @Min(1)
    @Max(100)
    @Default
    private int size = 20;
}
