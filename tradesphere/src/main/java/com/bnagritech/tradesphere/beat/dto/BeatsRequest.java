package com.bnagritech.tradesphere.beat.dto;

import com.bnagritech.tradesphere.beat.model.BeatRetailer;
import com.bnagritech.tradesphere.common.enums.BeatDay;
import com.bnagritech.tradesphere.common.enums.BeatFrequency;
import com.bnagritech.tradesphere.common.enums.BeatOwnerType;
import com.bnagritech.tradesphere.common.enums.RetailerType;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
@Data
public class BeatsRequest {
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
}
