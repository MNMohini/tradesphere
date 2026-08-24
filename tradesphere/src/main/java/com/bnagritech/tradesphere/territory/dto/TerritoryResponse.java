package com.bnagritech.tradesphere.territory.dto;

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
    private List<String> state;
    private List<String> city;
    private LocalDateTime updateAt;
    private LocalDateTime createAt;
    private String updateBy;
}
