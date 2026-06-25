package com.bnagritech.tradesphere.territory.model;

import com.bnagritech.tradesphere.territory.TerritoryType;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

public class Territory {
    @Id
    private String id;
    private String territoryId;
    private String territoryName;
    private TerritoryType territoryType;
    private String state;
    private String city;
    private String description;
    private Boolean active;
    private LocalDateTime updateAt;
    private LocalDateTime createdBy;
    private String updatedAt;
}
