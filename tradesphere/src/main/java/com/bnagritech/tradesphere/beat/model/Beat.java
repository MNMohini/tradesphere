package com.bnagritech.tradesphere.beat.model;

import com.bnagritech.tradesphere.common.enums.*;
import com.bnagritech.tradesphere.outlet.model.Outlet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "beat")
public class Beat {

    @Id
    private String id;
    private String beatId;
    private String territoryId;
    private String promoterId;
    private BeatStatus beatStatus;
    private String state;
    private String city;
    private List<String> outletIds;
    private List<BeatDay> beatDays;
    private BeatFrequency frequency;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

}
