package com.bnagritech.tradesphere.territory.service.impl;

import com.bnagritech.tradesphere.common.exception.ResourceAlreadyExistsException;
import com.bnagritech.tradesphere.common.exception.ResourceNotFoundException;
import com.bnagritech.tradesphere.common.exception.TerritoryNotFoundException;
import com.bnagritech.tradesphere.territory.dto.TerritoryRequest;
import com.bnagritech.tradesphere.territory.dto.TerritoryResponse;
import com.bnagritech.tradesphere.territory.model.Territory;
import com.bnagritech.tradesphere.territory.repository.TerritoryRepository;
import com.bnagritech.tradesphere.territory.service.TerritoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TerritoryServiceImpl implements TerritoryService {
    private final TerritoryRepository territoryRepository;

    @Override
    public TerritoryResponse createTerritory(TerritoryRequest request) {

            if (territoryRepository.findByTerritoryNameIgnoreCase(request.getTerritoryName()).isPresent()) {
                throw new ResourceAlreadyExistsException("Territory name already exists");
            }
            if (territoryRepository.findByTerritoryId(request.getTerritoryId()).isPresent()){
                throw new ResourceAlreadyExistsException("Territory already Exists");
            }

            Territory territory = Territory.builder()
                    .territoryId(request.getTerritoryId())
                    .territoryName(request.getTerritoryName())
                    .beatId(request.getBeatId() != null
                            ? new ArrayList<>(request.getBeatId())
                            : new ArrayList<>())
                    .state(new ArrayList<>(request.getState()))
                    .city(new ArrayList<>(request.getCity()))
                    .createAt(LocalDateTime.now())
                    .updateAt(LocalDateTime.now())
                    .build();

            Territory savedTerritory = territoryRepository.save(territory);
            return mapToResponse(savedTerritory);

        }

    @Override
    public List<TerritoryResponse> getAllTerritories() {

        try {
            return territoryRepository.findAll()
                    .stream()
                    .map(this::mapToResponse)
                    .toList();

        }
        catch (Exception e) {
            throw new ResourceNotFoundException("Unable to fetch territories");
        }
    }

    @Override
    public TerritoryResponse getTerritoryById(String territoryId) {

            Territory territory = territoryRepository.findByTerritoryId(territoryId)
                    .orElseThrow(() -> new TerritoryNotFoundException(
                                            "Territory not found with ID: " + territoryId));

            return mapToResponse(territory);

    }

    @Override
    public TerritoryResponse updateTerritory(String territoryId, TerritoryRequest request) {

            Territory territory = territoryRepository.findByTerritoryId(territoryId)
                            .orElseThrow(() -> new TerritoryNotFoundException(
                                            "Territory not found with ID: " + territoryId));
            if (!territory.getTerritoryId()
                    .equals(request.getTerritoryId())
                    && territoryRepository.existsTerritoryByTerritoryId(
                    request.getTerritoryId())) {

                throw new ResourceAlreadyExistsException("Territory ID already exists: "
                        + request.getTerritoryId());
            }

            if (!territory.getTerritoryName()
                .equals(request.getTerritoryName())
                && territoryRepository.existsTerritoryByTerritoryName(
                request.getTerritoryName())) {

            throw new ResourceAlreadyExistsException("Territory Name already exists: "
                    + request.getTerritoryId());
        }
            territory.setTerritoryId(request.getTerritoryId());
            territory.setTerritoryName(request.getTerritoryName());
            territory.setBeatId(request.getBeatId() != null
                            ? new ArrayList<>(request.getBeatId())
                            : new ArrayList<>());
            territory.setState(new ArrayList<>(request.getState()));
            territory.setCity(new ArrayList<>(request.getCity()));
            territory.setUpdateAt(LocalDateTime.now());

            Territory updatedTerritory = territoryRepository.save(territory);
            return mapToResponse(updatedTerritory);

        }

    @Override
    public void deleteTerritory(String territoryId) {
            Territory territory = territoryRepository.findByTerritoryId(territoryId)
                    .orElseThrow(() -> new TerritoryNotFoundException(
                                            "Territory not found with ID: " + territoryId));
            territoryRepository.delete(territory);
    }

    @Override
    public List<TerritoryResponse> getTerritoriesByState( String state) {

        List<Territory> territoryList= territoryRepository.findByStateIgnoreCase(Collections.singletonList(state));
        if(territoryList.isEmpty()){
            throw new ResourceNotFoundException("Resource Not Found");
        }
        return territoryList
                    .stream()
                    .map(this::mapToResponse)
                    .toList();

        }

    @Override
    public List<TerritoryResponse> getTerritoriesByCity(List<String> city) {
        List<Territory> territoryList= territoryRepository.findByCityIgnoreCase(city);
                if(territoryList.isEmpty()){
                    throw new ResourceNotFoundException("Resource Not Found");
                }
            return territoryList
                    .stream()
                    .map(this::mapToResponse)
                    .toList();

        }
    private  TerritoryResponse mapToResponse(Territory territory){
        return TerritoryResponse.builder()
                .territoryId(territory.getTerritoryId())
                .territoryName(territory.getTerritoryName())
                .state(territory.getState())
                .beatId(territory.getBeatId())
                .city(territory.getCity())
                .updateAt(LocalDateTime.now())
                .createAt(LocalDateTime.now())
                .build();
    }
}
