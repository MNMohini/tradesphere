package com.bnagritech.tradesphere.territory.service.impl;

import com.bnagritech.tradesphere.common.exception.ResourceNotFoundException;
import com.bnagritech.tradesphere.common.exception.TerritoryAlreadyExistException;
import com.bnagritech.tradesphere.common.exception.TerritoryNotFoundException;
import com.bnagritech.tradesphere.territory.dto.TerritoryRequest;
import com.bnagritech.tradesphere.territory.dto.TerritoryResponse;
import com.bnagritech.tradesphere.territory.model.Territory;
import com.bnagritech.tradesphere.territory.repository.TerritoryRepository;
import com.bnagritech.tradesphere.territory.service.TerritoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TerritoryServiceImpl implements TerritoryService {
    private final TerritoryRepository territoryRepository;

    @Override
    public TerritoryResponse createTerritory(TerritoryRequest request) {
        if (territoryRepository.existsTerritoryByTerritoryId(request.getTerritoryId())) {
            throw new TerritoryAlreadyExistException("This Territory is already exists in the System");
        }

        if (territoryRepository.existsTerritoryByTerritoryNameAndCityAndState(
                request.getTerritoryName().trim(),
                request.getCity().trim(),
                request.getState().trim())) {
            throw new TerritoryAlreadyExistException("This Territory is already exists in the System");
        }
        Territory territory = Territory.builder()
                .territoryId(request.getTerritoryId())
                .territoryName(request.getTerritoryName())
                .territoryType(request.getTerritoryType())
                .state(request.getState())
                .city(request.getCity())
                .description(request.getDescription())
                .active(request.getActive() != null ? request.getActive() : true)
                .createAt(LocalDateTime.now())
                .updateAt(LocalDateTime.now())
                .build();

        Territory savedTerritory = territoryRepository.save(territory);

        return mapToResponse(savedTerritory);
    }
    @Override
    public List<TerritoryResponse> getAllTerritories(){
        return territoryRepository.findAll()
                .stream()
                .map(this:: mapToResponse)
                .toList();

    }

    @Override
    public TerritoryResponse getTerritoryById(String id){
        Territory territory = territoryRepository.findById(id).orElseThrow(
                ()-> new ResourceNotFoundException("Territory Not Found"));
   return mapToResponse(territory);
    }

    @Override
    public TerritoryResponse updateTerritory(String id, TerritoryRequest request){
        Territory territory= territoryRepository.findById(id).orElseThrow(
                ()-> new ResourceNotFoundException("Territory Not Found"));

       territory.setTerritoryId(request.getTerritoryId());
       territory.setTerritoryName(request.getTerritoryName());
       territory.setState(request.getState());
       territory.setCity(request.getCity());
       territory.setDescription(request.getDescription());
       territory.setActive(request.getActive());
       territory.setUpdateAt(LocalDateTime.now());
        Territory updateTerritory = territoryRepository.save(territory);

        return mapToResponse(updateTerritory);
    }
    @Override
    public void deleteTerritory(String id){
        Territory territory= territoryRepository.findById(id).orElseThrow(
                ()-> new ResourceNotFoundException("Territory Not Found"));
        territoryRepository.delete(territory);
    }

    @Override
    public List<TerritoryResponse> getTerritoriesByState(String state) {
        List<Territory> territories = territoryRepository.findByStateIgnoreCase(state);
        if (territories.isEmpty()) {
            throw new TerritoryNotFoundException("No territory found in " + state);
        }
        return  territories.stream()
                .map(this::mapToResponse)
                .toList();

    }

    @Override
    public List<TerritoryResponse> getTerritoriesByCity(String city) {
        List<Territory> territories = territoryRepository.findByCityIgnoreCase(city);
        if (territories.isEmpty()) {
            throw new TerritoryNotFoundException("No territory found in " + city);
        }
        return  territories.stream()
                .map(this::mapToResponse)
                .toList();
    }


    private TerritoryResponse mapToResponse(Territory territory){
        return TerritoryResponse.builder()
                .territoryId(territory.getTerritoryId())
                .territoryName(territory.getTerritoryName())
                .territoryType(territory.getTerritoryType())
                .state(territory.getState())
                .city(territory.getCity())
                .description(territory.getDescription())
                .active(territory.getActive() != null ? territory.getActive() : true)
                .createAt(LocalDateTime.now())
                .updateAt(LocalDateTime.now())
                .build();
     }



}
