package com.bnagritech.tradesphere.territory.service;

import com.bnagritech.tradesphere.beat.dto.BeatsResponse;
import com.bnagritech.tradesphere.beat.model.Beat;
import com.bnagritech.tradesphere.territory.dto.TerritoryRequest;
import com.bnagritech.tradesphere.territory.dto.TerritoryResponse;

import java.util.List;

public interface TerritoryService {
    TerritoryResponse createTerritory(TerritoryRequest request);
    List<TerritoryResponse>getAllTerritories();
    TerritoryResponse getTerritoryById(String id);
    TerritoryResponse updateTerritory(String id,TerritoryRequest request);
    void deleteTerritory(String id);
    List<TerritoryResponse>getTerritoriesByState(String state);
    List<TerritoryResponse>getTerritoriesByCity(String city);
    TerritoryResponse addBeatToTerritory(String territoryId, Beat beatId);
    TerritoryResponse removeBeatFromTerritory(String territoryId, Beat beatId);
}
