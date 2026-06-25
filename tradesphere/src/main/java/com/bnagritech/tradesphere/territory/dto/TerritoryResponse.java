package com.bnagritech.tradesphere.territory.dto;

import com.bnagritech.tradesphere.territory.TerritoryType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@Builder
public class TerritoryResponse {
    private String id;
    private String territoryId;
    private String territoryName;
    private TerritoryType territoryType;
    private String state;
    private String city;
    private String description;
    private Boolean active;
    private LocalDateTime updateAt;
    private LocalDateTime createAt;
}
