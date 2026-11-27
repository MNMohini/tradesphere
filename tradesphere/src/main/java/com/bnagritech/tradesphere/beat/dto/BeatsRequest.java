package com.bnagritech.tradesphere.beat.dto;

import com.bnagritech.tradesphere.common.enums.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BeatsRequest {

    private String beatId;
    private String territoryId;
    private String state;
    private String city;
    private List<String> outletIds;
    @NotEmpty(message = "Select at least one visit day")
    private List<BeatDay> beatDays;
    private BeatFrequency frequency;
    private BeatStatus beatStatus;
}
