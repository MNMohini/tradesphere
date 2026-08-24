package com.bnagritech.tradesphere.beat.dto;

import com.bnagritech.tradesphere.common.enums.*;
import com.bnagritech.tradesphere.outlet.model.Outlet;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
@Data
@Builder
public class BeatsResponse {
    @Id
    private String id;
    private String beatId;
    private String territoryId;
    private String promoterId;
    private String state;
    private String city;
    private List<String> outletIds;
    private List<BeatDay> beatDays;
    private BeatFrequency frequency;
    private BeatStatus beatStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
