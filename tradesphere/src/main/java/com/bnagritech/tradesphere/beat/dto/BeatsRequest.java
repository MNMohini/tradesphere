package com.bnagritech.tradesphere.beat.dto;

import com.bnagritech.tradesphere.common.enums.*;
import com.bnagritech.tradesphere.outlet.model.Outlet;
import com.bnagritech.tradesphere.retailer.model.Retailer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BeatsRequest {

    private String beatId;
    private String promoterId;
    private String territoryId;
    private String state;
    private String city;
    private List<String> outletIds;
    private List<BeatDay> beatDays;
    private BeatFrequency frequency;
    private BeatStatus beatStatus;
}
