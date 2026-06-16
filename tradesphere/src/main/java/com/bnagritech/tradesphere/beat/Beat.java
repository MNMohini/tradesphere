package com.bnagritech.tradesphere.beat;

import java.time.LocalDateTime;
import java.util.List;

public class Beat {
    private String beatId;
    private String beatCode;
    private String beatName;
    private String territoryId;
    private String assignedEmployeeID;
    private String description;
    private List<String> visitDay;
    private Integer retailerCount;
    private Boolean active;
    private LocalDateTime updatedAt;
    private LocalDateTime createdAt;
    private String createdBy;
    private String updatedBy;
}
