package com.bnagritech.tradesphere.salesrepresentativemodule.service.impl;
import com.bnagritech.tradesphere.common.enums.UserStatus;
import com.bnagritech.tradesphere.common.exception.ResourceAlreadyExistsException;
import com.bnagritech.tradesphere.common.exception.ResourceNotFoundException;
import com.bnagritech.tradesphere.common.exception.TerritoryNotFoundException;
import com.bnagritech.tradesphere.salesrepresentativemodule.dto.SRRequest;
import com.bnagritech.tradesphere.salesrepresentativemodule.dto.SRResponse;
import com.bnagritech.tradesphere.salesrepresentativemodule.model.SalesRepresentative;
import com.bnagritech.tradesphere.salesrepresentativemodule.repository.SRRepository;
import com.bnagritech.tradesphere.salesrepresentativemodule.service.SRService;
import com.bnagritech.tradesphere.territory.model.Territory;
import com.bnagritech.tradesphere.territory.repository.TerritoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
@Service
@RequiredArgsConstructor
public class SRServiceImpl implements SRService {
    private final SRRepository srRepository;
    private final TerritoryRepository territoryRepository;

    @Override
    public SRResponse createSR(SRRequest request) {

        Territory territory = territoryRepository.findByTerritoryId(request.getTerritoryId())
                .orElseThrow(()-> new ResourceNotFoundException(
                        "Territory Id doesn't Exists"));

        if (srRepository.existsBySRId(request.getSrId())) {
            throw new ResourceAlreadyExistsException("SR ID " + request.getSrId() + " already exists.");
        }
        if(srRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("Email already exists");
        }
        if(srRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new ResourceAlreadyExistsException("Phone number already exists");
        }
        SalesRepresentative salesRepresentative =  SalesRepresentative.builder()
                .SRId(request.getSrId())
                .SRName(request.getSrName())
                .userName(request.getUserName())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .territoryId(territory.getTerritoryId())
                .city(request.getCity())
                .state(request.getState())
                .status(request.getStatus())
                .CreatedAt(LocalDateTime.now())
                .build();
       SalesRepresentative savedRepresentative = srRepository.save(salesRepresentative);
        return mapToResponse(savedRepresentative);
    }

    @Override
    public List<SRResponse> getAllSRs() {
        return srRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public SRResponse updateSR(String srId, SRRequest request) {
        SalesRepresentative salesRepresentative = srRepository.findBySRId(srId)
                .orElseThrow(()-> new ResourceNotFoundException(
                        "Sales Representative doesn't Exists with this Id"));
        salesRepresentative.setEmail(request.getEmail());
        salesRepresentative.setPhoneNumber(request.getPhoneNumber());
        salesRepresentative.setUserName(request.getUserName());
        salesRepresentative.setStatus(request.getStatus());
        salesRepresentative.setUpdatedAt(LocalDateTime.now());
        salesRepresentative.setTerritoryId(request.getTerritoryId());
        SalesRepresentative updatedRepresentative = srRepository.save(salesRepresentative);
        return mapToResponse(updatedRepresentative);
    }

    @Override
    public void deleteSR(String srId) {
        SalesRepresentative salesRepresentative = srRepository.findBySRId(srId)
                .orElseThrow(()-> new ResourceNotFoundException(
                        "Sales Representative doesn't Exists with this Id"));
        srRepository.delete(salesRepresentative);
    }

    @Override
    public SRResponse getSRById(String srId) {
        SalesRepresentative salesRepresentative = srRepository.findBySRId(srId)
                .orElseThrow(()-> new ResourceNotFoundException(
                        "Sales Representative doesn't Exists with this Id"));
        return mapToResponse(salesRepresentative);
    }

    @Override
    public SRResponse getSRByEmail(String email) {
        SalesRepresentative salesRepresentative = srRepository.findByEmail(email)
                .orElseThrow(()-> new ResourceNotFoundException(
                        "Sales Representative doesn't Exists with this email"));
        return mapToResponse(salesRepresentative);
    }

    @Override
    public SRResponse getSRByPhoneNumber(String phoneNumber) {
        SalesRepresentative salesRepresentative = srRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(()-> new ResourceNotFoundException(
                        "Not Found Sales Representative with Phone Number " + phoneNumber));
        return mapToResponse(salesRepresentative);
    }

    @Override
    public List<SRResponse> getAllSRsByTerritory(String territoryId) {
        List<SalesRepresentative> salesRepresentatives = srRepository.findByTerritoryId(territoryId);
        if(salesRepresentatives.isEmpty()){
            throw new TerritoryNotFoundException("Territory Id doesn't Exists");
        }
        return salesRepresentatives.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<SRResponse> getAllSRsByStatus(UserStatus status) {
        List<SalesRepresentative> salesRepresentatives = srRepository.findByStatus(status);
        if(salesRepresentatives.isEmpty()){
            throw new ResourceNotFoundException("Not found any Representative with" + status);
        }
        return salesRepresentatives.stream()
                .map(this::mapToResponse)
                .toList();
    }
    private  SRResponse mapToResponse(SalesRepresentative salesRepresentative) {
        SRResponse response = new SRResponse();
        response.setSrId(salesRepresentative.getSRId());
        response.setSrName(salesRepresentative.getSRName());
        response.setUserName(salesRepresentative.getUserName());
        response.setPhoneNumber(salesRepresentative.getPhoneNumber());
        response.setEmail(salesRepresentative.getEmail());
        response.setTerritoryId(salesRepresentative.getTerritoryId());
        response.setCity(salesRepresentative.getCity());
        response.setState(salesRepresentative.getState());
        response.setStatus(salesRepresentative.getStatus());
        return response;
    }
}
