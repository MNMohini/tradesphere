package com.bnagritech.tradesphere.territory.dto;

import lombok.Data;

@Data
public class TerritoryRequest {

    private String territoryId;
    private String territoryName;
    private String state;
    private String city;
    private String description;
    private Boolean active;
    private String territoryType;
}
