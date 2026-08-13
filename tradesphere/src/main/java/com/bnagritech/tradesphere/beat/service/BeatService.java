package com.bnagritech.tradesphere.beat.service;

import com.bnagritech.tradesphere.beat.dto.BeatsRequest;
import com.bnagritech.tradesphere.beat.dto.BeatsResponse;
import com.bnagritech.tradesphere.common.enums.ApprovalStatus;
import com.bnagritech.tradesphere.common.enums.BeatDay;
import com.bnagritech.tradesphere.common.enums.BeatStatus;

import java.util.List;

public interface BeatService {
    // BASIC CRUD
    BeatsResponse createBeat(BeatsRequest request);
    BeatsResponse updateBeat(String beatId, BeatsRequest request);
    BeatsResponse getBeatById(String beatId);
    List<BeatsResponse> getAllBeats();
    void deleteBeat(String beatId);
    // TERRITORY
    List<BeatsResponse> getBeatsByTerritory(String territoryId);
    //EMPLOYEE BEATS
    List<BeatsResponse> getEmployeeBeats(String employeeId);
    List<BeatsResponse> getEmployeeDayBeats(String employeeId, BeatDay beatDay);
    // RETAILER MAPPING
    BeatsResponse assignRetailerToBeat(String beatId, String retailerId);
    BeatsResponse removeRetailerFromBeat(String beatId, String retailerId);
    BeatsResponse getBeatByRetailer(String retailerId);
    // APPROVAL
    BeatsResponse approveBeat(String beatId, String managerId);
    BeatsResponse rejectBeat(String beatId, String managerId);
    List<BeatsResponse> searchByCity(String city);
    List<BeatsResponse> searchByState(String state);
    // DASHBOARD FILTERS
    List<BeatsResponse> getByStatus(BeatStatus status);
    List<BeatsResponse> getByBeatType(String beatType);
    List<BeatsResponse> getByApprovalStatus(ApprovalStatus approvalStatus);

}

