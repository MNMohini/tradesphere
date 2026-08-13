package com.bnagritech.tradesphere.beat.dto;

import com.bnagritech.tradesphere.beat.model.BeatRetailer;
import com.bnagritech.tradesphere.common.enums.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
@Data
@Builder
public class BeatsResponse {

    // Basic Details
    private String beatId;
    private String beatName;
    private String beatCode;
    private String description;
    private String beatType;
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

}
