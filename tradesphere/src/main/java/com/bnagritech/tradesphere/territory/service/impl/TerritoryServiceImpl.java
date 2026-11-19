package com.bnagritech.tradesphere.territory.service.impl;

import com.bnagritech.tradesphere.auth.model.User;
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

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Service
@RequiredArgsConstructor
public class TerritoryServiceImpl implements TerritoryService {
    private final TerritoryRepository territoryRepository;

    @Override
    public TerritoryResponse createTerritory(TerritoryRequest request ) {

        if (territoryRepository.existsTerritoryByTerritoryName(
                request.getTerritoryName())) {
            throw new ResourceAlreadyExistsException("Territory name already exists");
        }
        if (territoryRepository.existsTerritoryByTerritoryId(
                request.getTerritoryId())) {
            throw new ResourceAlreadyExistsException("Territory ID already exists");
        }
        List<String> beatIds = request.getBeatId() != null ? request.getBeatId() : new ArrayList<>();
        // Check duplicate beats inside the request
        Set<String> uniqueBeatIds = new HashSet<>();
        for (String beatId : beatIds) {
            if (!uniqueBeatIds.add(beatId)) {
                throw new ResourceAlreadyExistsException(
                        "Duplicate beat ID in request: " + beatId);}
            // Check whether beat is already assigned to another territory
            if (territoryRepository.existsByBeatIdContaining(beatId)) {
                throw new ResourceAlreadyExistsException(
                        "Beat ID " + beatId + " is already assigned to another territory");
            }
        }
        List<String> states = request.getState()!= null ? request.getState(): new ArrayList<>();
        Set<String> uniqueStates = new HashSet<>();
        for (String state : states) {
            if (!uniqueStates.add(state)) {
                throw new ResourceAlreadyExistsException("Duplicate state in request: " + state);}
                if (territoryRepository.existsByStateIn(state)){
                    throw new ResourceAlreadyExistsException("State " + state + " is already assigned to another territory");
                }
        }
        List<String> cities = request.getCity()!= null ? request.getCity(): new ArrayList<>();
        Set<String> uniqueCities = new HashSet<>();
        for (String city : cities) {
            if (!uniqueCities.add(city)) {
                throw new ResourceAlreadyExistsException("Duplicate city in request: " + city);
            }
            if (territoryRepository.existsByCityIn(city)) {
                throw new ResourceAlreadyExistsException("State " + city + " is already assigned to another territory");
            }
        }

            Territory territory = Territory.builder()
                .territoryId(request.getTerritoryId())
                .territoryName(request.getTerritoryName())
                .beatId(new ArrayList<>(beatIds))
                .state(request.getState() != null
                        ? new ArrayList<>(request.getState())
                        : new ArrayList<>())
                .city(request.getCity() != null
                        ? new ArrayList<>(request.getCity())
                        : new ArrayList<>())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(Instant.now())
                .build();

        Territory savedTerritory =
                territoryRepository.save(territory);

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
            territory.setUpdatedAt(Instant.now());

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

    private  TerritoryResponse mapToResponse(Territory territory){
        return TerritoryResponse.builder()
                .territoryId(territory.getTerritoryId())
                .territoryName(territory.getTerritoryName())
                .state(territory.getState())
                .beatId(territory.getBeatId())
                .city(territory.getCity())
                .build();
    }
}
