package com.bnagritech.tradesphere.scheme.model;

import com.bnagritech.tradesphere.common.enums.SchemeStatus;
import com.bnagritech.tradesphere.common.enums.SchemeType;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "schemes")

public class Scheme {

    @Id
    private String id;

    private String schemeCode;

    private String schemeName;

    private String description;

    private SchemeType schemeType;

    private LocalDate startDate;

    private LocalDate endDate;

    private List<String> productIds;
    private SchemeStatus status;

    private Instant createdAt;

    private Instant updatedAt;

    private String createdBy;

    private String updatedBy;
}