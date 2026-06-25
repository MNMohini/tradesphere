package com.bnagritech.tradesphere.territory.dto;

import com.bnagritech.tradesphere.territory.TerritoryType;
import lombok.Data;

@Data
public class TerritoryRequest {

    private String territoryCode;
    private String territoryName;
    private String state;
    private String city;
    private String description;
    private Boolean active;
    private TerritoryType territoryType;

}
