package com.bnagritech.tradesphere.territory.service;

import com.bnagritech.tradesphere.beat.dto.BeatsResponse;
import com.bnagritech.tradesphere.beat.model.Beat;
import com.bnagritech.tradesphere.common.enums.BeatStatus;
import com.bnagritech.tradesphere.territory.dto.TerritoryRequest;
import com.bnagritech.tradesphere.territory.dto.TerritoryResponse;

import java.util.List;

public interface TerritoryService {
    TerritoryResponse createTerritory(TerritoryRequest request);
    List<TerritoryResponse>getAllTerritories();
    TerritoryResponse getTerritoryById(String territoryId);
    TerritoryResponse updateTerritory(String territoryId,TerritoryRequest request);
    void deleteTerritory(String territoryId);
    List<TerritoryResponse>getTerritoriesByState(String  state);
    List<TerritoryResponse>getTerritoriesByCity(List<String> city);

}
