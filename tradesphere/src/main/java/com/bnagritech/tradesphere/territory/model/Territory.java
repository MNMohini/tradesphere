package com.bnagritech.tradesphere.territory.model;

import com.bnagritech.tradesphere.beat.model.Beat;
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
@Document(collection = "territories")
public class Territory {
    @Id
    private String id;
    private String territoryId;
    private String territoryName;
    private List<Beat> beatId;
    private String territoryType;
    private String state;
    private String city;
    private String description;
    private Boolean active;
    private LocalDateTime updateAt;
    private LocalDateTime createAt;
    private String updatedAt;
}
