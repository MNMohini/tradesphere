package com.bnagritech.tradesphere.retailer.service.impl;

import com.bnagritech.tradesphere.common.enums.RetailerStatus;
import com.bnagritech.tradesphere.common.exception.ResourceAlreadyExistsException;
import com.bnagritech.tradesphere.common.exception.ResourceNotFoundException;
import com.bnagritech.tradesphere.promoter.model.Promoter;
import com.bnagritech.tradesphere.retailer.dto.RetailerRequest;
import com.bnagritech.tradesphere.retailer.dto.RetailerResponse;
import com.bnagritech.tradesphere.retailer.model.Retailer;
import com.bnagritech.tradesphere.retailer.repository.RetailerRepository;
import com.bnagritech.tradesphere.retailer.service.RetailerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RetailerServiceImpl implements RetailerService {

    private final RetailerRepository retailerRepository;

    @Override
    public RetailerResponse createRetailer(RetailerRequest request) {
        if(retailerRepository.existsByEmail(request.getEmail())){
            throw new ResourceAlreadyExistsException("Resource already exists");
        }
        if(retailerRepository.existsByPhoneNumber(request.getPhoneNumber())){
            throw new ResourceAlreadyExistsException("Resource already exists");
        }
        if (retailerRepository.existsByRetailerId(request.getRetailerId())){
            throw new ResourceAlreadyExistsException("Resource already exists");
        }
        if (retailerRepository.existsByShopNameAndCityAndState(
                request.getShopName(),
                request.getCity(),
                request.getState())){
            throw new ResourceAlreadyExistsException("Resource already exists");
        }
            Retailer retailer = Retailer.builder()
                    .retailerId(request.getRetailerId())
                    .shopName(request.getShopName())
                    .ownerName(request.getOwnerName())
                    .phoneNumber(request.getPhoneNumber())
                    .alternateContactNumber(request.getAlternateContactNumber())
                    .email(request.getEmail())
                    .address(request.getAddress())
                    .territoryId(request.getTerritoryId())
                    .beatId(request.getBeatId())
                    .promoterId(request.getPromoterId())
                    .retailerType(request.getRetailerType())
                    .retailerStatus(request.getRetailerStatus())
                    .gstNumber(request.getGstNumber())
                    .panNumber(request.getPanNumber())
                    .longitude(request.getLongitude())
                    .latitude(request.getLatitude())
                    .creditDays(request.getCreditDays())
                    .creditLimits(request.getCreditLimits())
                    .createdAt(request.getCreatedAt())
                    .updatedAt(request.getUpdatedAt())
                    .createdBy(request.getCreatedBy())
                    .updatedBy(request.getUpdatedBy())
                    .retailerStatus(request.getRetailerStatus())
                    .build();
        Retailer savedRetailer = retailerRepository.save(retailer);
        return mapToResponse(savedRetailer);
    }

    @Override
    public RetailerResponse getRetailerById(String retailerId) {
        Retailer retailer = retailerRepository.findByRetailerId(retailerId)
                .orElseThrow(
                        ()->
                                new ResourceNotFoundException(
                                        "Retailer not found with " +retailerId +" id"));

        return mapToResponse(retailer);
    }

    @Override
    public RetailerResponse updateRetailer(String retailerId, RetailerRequest request) {
        Retailer retailer = retailerRepository.findByRetailerId(retailerId)
                .orElseThrow(
                        ()->
                                new ResourceNotFoundException(
                                        "Retailer not found with " +retailerId +" id"));
        return null;
    }

    @Override
    public RetailerResponse getRetailerByPhoneNumber(long phoneNumber) {
        Retailer retailer = retailerRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(
                        ()->
                                 new ResourceNotFoundException(
                                         "Retailer not found with " +phoneNumber
                                 )
                );
        return mapToResponse(retailer);
    }

    @Override
    public RetailerResponse getRetailerByEmail(String email) {
        Retailer retailer = retailerRepository.findByEmail(email)
                .orElseThrow(
                        ()->
                                new ResourceNotFoundException(
                                        "Retailer not found with " +email ));

        return mapToResponse(retailer);
    }

    @Override
    public RetailerResponse updateRetailerStatus(String retailerId, RetailerRequest request) {
        Retailer retailer = retailerRepository.findByRetailerId(retailerId)
                .orElseThrow(
                        ()-> new ResourceNotFoundException(
                                "Retailer not found with " +retailerId + " id"));
         retailer.setRetailerStatus(request.getRetailerStatus());
         Retailer updatedRetailer= retailerRepository.save(retailer);

        return mapToResponse(updatedRetailer);
    }

    @Override
    public RetailerResponse assignRetailer(String retailerId, RetailerRequest request) {
        return null;
    }

    @Override
    public void deleteRetailer(String retailerId) {

    }

    @Override
    public List<RetailerResponse> getRetailerByTerritory(String territoryId) {
        List<Retailer> retailerList = retailerRepository.findByTerritoryId(territoryId);
        return retailerList.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<RetailerResponse> getRetailerByShopName(String shopName) {
        List<Retailer> retailerList = retailerRepository.findByShopNameContainingIgnoreCase(shopName);
        return retailerList.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<RetailerResponse> getRetailerByOwnerName(String ownerName) {
        List<Retailer> retailerList = retailerRepository.findByOwnerNameContainingIgnoreCase(ownerName);
        return  retailerList.stream()
                .map(this::mapToResponse)
                .toList();
    }


    @Override
    public List<RetailerResponse> getRetailerByEmployeeId(String employeeId) {
        List<Retailer> retailerList = retailerRepository.findRetailerByEmployeeId(employeeId);
        return retailerList.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<RetailerResponse> getRetailerByCity(String city) {
        List<Retailer> retailerList = retailerRepository.findByCity(city);
        return retailerList.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<RetailerResponse> getRetailerByState(String state) {
       List<Retailer> retailerList = retailerRepository.findByState(state);
        return retailerList.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<RetailerResponse> getRetailerByRetailerStatus(RetailerStatus status) {
        List<Retailer> retailerList = retailerRepository.findRetailerByRetailerStatus(status);
        return retailerList.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<RetailerResponse> getAllRetailer() {
        return List.of();
    }

    private RetailerResponse mapToResponse(Retailer retailer){
        RetailerResponse retailerResponse = new RetailerResponse();
        retailerResponse.setRetailerId(retailer.getRetailerId());
        retailerResponse.setShopName(retailer.getShopName());
        retailerResponse.setOwnerName(retailer.getOwnerName());
        retailerResponse.setPhoneNumber(retailer.getPhoneNumber());
        retailerResponse.setAlternateContactNumber(retailer.getAlternateContactNumber());
        retailerResponse.setEmail(retailer.getEmail());
        retailerResponse.setAddress(retailer.getAddress());
        retailerResponse.setTerritoryId(retailer.getTerritoryId());
        retailerResponse.setBeatId(retailer.getBeatId());
        retailerResponse.setPromoterId(retailer.getPromoterId());
        retailerResponse.setRetailerType(retailer.getRetailerType());
        retailerResponse.setGstNumber(retailer.getGstNumber());
        retailerResponse.setPanNumber(retailer.getPanNumber());
        retailerResponse.setLatitude(retailer.getLatitude());
        retailerResponse.setLongitude(retailer.getLongitude());
        retailerResponse.setCreditDays(retailer.getCreditDays());
        retailerResponse.setCreditLimits(retailer.getCreditLimits());
        retailerResponse.setCreatedAt(retailer.getCreatedAt());
        retailerResponse.setUpdatedAt(retailer.getUpdatedAt());
        retailerResponse.setRetailerStatus(retailer.getRetailerStatus());
        retailerResponse.setCreatedBy(retailer.getCreatedBy());
        retailerResponse.setUpdatedBy(retailer.getUpdatedBy());

        return retailerResponse;
    }
}
