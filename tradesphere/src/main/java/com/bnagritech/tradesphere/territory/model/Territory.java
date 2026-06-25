package com.bnagritech.tradesphere.territory.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
@Data
@Builder
public class Territory {
    @Id
    private String id;
    private String territoryId;
    private String territoryName;
    private String territoryType;
    private String state;
    private String city;
    private String description;
    private Boolean active;
    private LocalDateTime updateAt;
    private LocalDateTime createAt;
    private String updatedAt;
}
