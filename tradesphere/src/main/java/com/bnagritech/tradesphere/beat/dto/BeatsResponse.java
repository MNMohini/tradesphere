package com.bnagritech.tradesphere.beat.dto;

import com.bnagritech.tradesphere.common.enums.*;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
@Data
@Builder
public class BeatsResponse {

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
