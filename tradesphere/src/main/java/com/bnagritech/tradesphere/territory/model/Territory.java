package com.bnagritech.tradesphere.territory.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "territories")
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
