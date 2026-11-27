package com.bnagritech.tradesphere.visit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VisitorAttendancePageResponse {

    private List<VisitorAttendanceItemResponse> items;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
