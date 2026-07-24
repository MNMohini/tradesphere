package com.bnagritech.tradesphere.visit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VisitDashboardResponse {

    private long todayVisits;
    private long completedToday;
    private long pendingToday;
    private long checkedIn;
    private long totalOrdersBooked;
    private double totalCollection;
    private double productivityPercentage;

}
