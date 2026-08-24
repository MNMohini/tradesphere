package com.bnagritech.tradesphere.territory.dto;

import com.bnagritech.tradesphere.beat.model.Beat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class TerritoryResponse {
    private String id;
    private String territoryId;
    private String territoryName;
    private List<Beat> beatId;
    private List<String> state;
    private List<String> city;
    private LocalDateTime updateAt;
    private LocalDateTime createAt;
    private String updateBy;
}
