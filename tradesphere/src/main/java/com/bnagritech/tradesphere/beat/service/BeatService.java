package com.bnagritech.tradesphere.beat.service;

import com.bnagritech.tradesphere.beat.dto.BeatsRequest;
import com.bnagritech.tradesphere.beat.dto.BeatsResponse;
import com.bnagritech.tradesphere.beat.model.Beat;
import com.bnagritech.tradesphere.common.enums.ApprovalStatus;
import com.bnagritech.tradesphere.common.enums.BeatDay;
import com.bnagritech.tradesphere.common.enums.BeatStatus;

import java.util.List;

public interface BeatService {
    // BASIC CRUD
    BeatsResponse createBeat(BeatsRequest request);
    BeatsResponse updateBeat(String beatId, BeatsRequest request);
    BeatsResponse updateBeatStatus(String beatId, BeatsRequest request);
    BeatsResponse getBeatById(String beatId);
    List<BeatsResponse> getAllBeats();
    // TERRITORY
    List<BeatsResponse> getBeatsByTerritory(String territoryId);
    List<BeatsResponse> searchByCity(String city);
    List<BeatsResponse> searchByState(String state);
    // DASHBOARD FILTERS
    List<BeatsResponse> getByBeatStatus(BeatStatus status);
    void deleteBeat(String beatId);
}

