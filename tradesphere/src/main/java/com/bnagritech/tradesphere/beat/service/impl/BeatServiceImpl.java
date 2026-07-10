package com.bnagritech.tradesphere.beat.service.impl;

import com.bnagritech.tradesphere.beat.dto.BeatsRequest;
import com.bnagritech.tradesphere.beat.dto.BeatsResponse;
import com.bnagritech.tradesphere.beat.service.BeatService;
import com.bnagritech.tradesphere.common.enums.ApprovalStatus;
import com.bnagritech.tradesphere.common.enums.BeatDay;
import com.bnagritech.tradesphere.common.enums.BeatStatus;
import com.bnagritech.tradesphere.common.enums.RetailerType;

import java.util.List;

public class BeatServiceImpl implements BeatService {
    @Override
    public BeatsResponse createBeat(BeatsRequest request) {
        return null;
    }

    @Override
    public BeatsResponse updateBeat(String beatId, BeatsRequest request) {
        return null;
    }

    @Override
    public BeatsResponse getBeatById(String beatId) {
        return null;
    }

    @Override
    public List<BeatsResponse> getAllBeats() {
        return List.of();
    }

    @Override
    public void deleteBeat(String beatId) {

    }

    @Override
    public List<BeatsResponse> getBeatsByTerritory(String territoryId) {
        return List.of();
    }

    @Override
    public List<BeatsResponse> getEmployeeBeats(String employeeId) {
        return List.of();
    }

    @Override
    public List<BeatsResponse> getEmployeeDayBeats(String employeeId, BeatDay beatDay) {
        return List.of();
    }

    @Override
    public BeatsResponse assignRetailerToBeat(String beatId, String retailerId) {
        return null;
    }

    @Override
    public BeatsResponse removeRetailerFromBeat(String beatId, String retailerId) {
        return null;
    }

    @Override
    public BeatsResponse getBeatByRetailer(String retailerId) {
        return null;
    }

    @Override
    public BeatsResponse approveBeat(String beatId, String managerId) {
        return null;
    }

    @Override
    public BeatsResponse rejectBeat(String beatId, String managerId) {
        return null;
    }

    @Override
    public List<BeatsResponse> searchByCity(String city) {
        return List.of();
    }

    @Override
    public List<BeatsResponse> searchByState(String state) {
        return List.of();
    }

    @Override
    public List<BeatsResponse> getByStatus(BeatStatus status) {
        return List.of();
    }

    @Override
    public List<BeatsResponse> getByBeatType(RetailerType beatType) {
        return List.of();
    }

    @Override
    public List<BeatsResponse> getByApprovalStatus(ApprovalStatus approvalStatus) {
        return List.of();
    }
}
