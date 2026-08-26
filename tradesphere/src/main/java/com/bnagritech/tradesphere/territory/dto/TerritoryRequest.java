package com.bnagritech.tradesphere.territory.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

@Data
public class TerritoryRequest {
    @NotBlank(message= "required fields")
    private String territoryId;
    @NotBlank(message= "required fields")
    private String territoryName;
    private List<String> beatId;
    private List<String> state;
    private List<String> city;

}
