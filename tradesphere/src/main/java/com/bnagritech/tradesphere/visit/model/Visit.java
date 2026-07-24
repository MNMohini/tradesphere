package com.bnagritech.tradesphere.visit.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "visits")
public class Visit {
    @Id
    private String id;

}
