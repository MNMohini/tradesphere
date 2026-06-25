package com.bnagritech.tradesphere.territory.service;

import com.bnagritech.tradesphere.territory.dto.TerritoryRequest;
import com.bnagritech.tradesphere.territory.dto.TerritoryResponse;

import java.util.List;

public interface TerritoryService {
    TerritoryResponse createTerritory(TerritoryRequest request);
    List<TerritoryResponse>getAllTerritories();
    TerritoryResponse getTerritoryById(String id);
    TerritoryResponse updateTerritory(String id,TerritoryRequest request);
    void deleteTerritory(String territoryId);
}
