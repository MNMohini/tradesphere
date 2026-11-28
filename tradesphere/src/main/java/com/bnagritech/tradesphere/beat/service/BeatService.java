package com.bnagritech.tradesphere.beat.service;

import com.bnagritech.tradesphere.beat.dto.BeatsRequest;
import com.bnagritech.tradesphere.beat.dto.BeatsResponse;
import com.bnagritech.tradesphere.auth.model.User;
import com.bnagritech.tradesphere.auth.dto.UserSummaryResponse;
import com.bnagritech.tradesphere.common.enums.BeatStatus;

import java.util.List;

public interface BeatService {
    // BASIC CRUD
    BeatsResponse createBeat(BeatsRequest request);
    BeatsResponse updateBeat(String beatId, BeatsRequest request);
    BeatsResponse updateBeatStatus(String beatId, BeatsRequest request);
    BeatsResponse getBeatById(String beatId);
    List<BeatsResponse> getAllBeats();
    List<BeatsResponse> getMyBeats(User currentUser);
    List<BeatsResponse> getAssignableBeats(User currentUser);
    List<UserSummaryResponse> getAssignableUsers(User currentUser);
    BeatsResponse assignBeat(String beatId, String userId, User currentUser);
    // TERRITORY
    List<BeatsResponse> getBeatsByTerritory(String territoryId);
    List<BeatsResponse> searchByCity(String city);
    List<BeatsResponse> searchByState(String state);
    // DASHBOARD FILTERS
    List<BeatsResponse> getByBeatStatus(BeatStatus status);
    void deleteBeat(String beatId);
}

