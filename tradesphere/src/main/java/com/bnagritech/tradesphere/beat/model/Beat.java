package com.bnagritech.tradesphere.beat.model;

import com.bnagritech.tradesphere.common.enums.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "beats")
public class Beat {

    @Id
    private String beatId;
    // Basic Details
    private String beatName;
    private String beatCode;
    private String description;
    private RetailerType beatType;
    // Territory
    private String territoryId;
    // Employee Assignment
    private String assignedEmployeeId;
    private BeatOwnerType beatOwnerType;
    private String managerId;
    // Location
    private String state;
    private String city;
    private String area;
    private String pinCode;
    private Double latitude;
    private Double longitude;
    // Retailers in this Beat
    private List<BeatRetailer> retailers;
    private Integer totalRetailers;
    // Schedule
    private List<BeatDay> beatDays;
    private BeatFrequency frequency;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    // Route Information
    private Double estimatedDistanceKm;
    private Integer estimatedTravelTimeMinutes;
    // Approval Flow
    private ApprovalStatus approvalStatus;
    private String approvedBy;
    private LocalDateTime approvedAt;
    // Status
    private BeatStatus status;
    private Boolean isDeleted;
    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
