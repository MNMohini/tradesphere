package com.bnagritech.tradesphere.beat.repository;

import com.bnagritech.tradesphere.beat.model.Beat;
import com.bnagritech.tradesphere.common.enums.ApprovalStatus;
import com.bnagritech.tradesphere.common.enums.BeatDay;
import com.bnagritech.tradesphere.common.enums.BeatStatus;
import com.bnagritech.tradesphere.common.enums.RetailerType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BeatRepository extends MongoRepository<Beat, String> {
    // DUPLICATE VALIDATION
    boolean existsByBeatNameAndTerritoryIdAndBeatCode(String beatName, String territoryId, String beatCode);
    // Unique beat code validation
    boolean existsByBeatCode(String beatCode);
    // Get single active beat
    Optional<Beat> findByBeatId(String beatId);
    // TERRITORY BASED OPERATIONS
    List<Beat> findByTerritoryId(String territoryId);
    // EMPLOYEE BEAT
    List<Beat> findByAssignedEmployeeId(String employeeId);
    // Employee day wise route
    List<Beat> findByAssignedEmployeeIdAndBeatDaysContaining(String employeeId, BeatDay beatDay);
    // RETAILER BEAT MAPPING
    Optional<Beat> findByRetailersRetailerId(String retailerId);
    // LOCATION SEARCH
    List<Beat> findByCityIgnoreCase(String city);
    List<Beat> findByStateIgnoreCase(String state);
    // FILTERS FOR DASHBOARD
    List<Beat> findByStatus(BeatStatus status);
    List<Beat> findByBeatType(RetailerType beatType);
    List<Beat> findByApprovalStatus(ApprovalStatus approvalStatus);
}
