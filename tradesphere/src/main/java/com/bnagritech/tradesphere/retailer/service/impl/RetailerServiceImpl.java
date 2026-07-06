package com.bnagritech.tradesphere.retailer.service.impl;

import com.bnagritech.tradesphere.common.exception.ResourceAlreadyExistsException;
import com.bnagritech.tradesphere.retailer.dto.RetailerRequest;
import com.bnagritech.tradesphere.retailer.dto.RetailerResponse;
import com.bnagritech.tradesphere.retailer.model.Retailer;
import com.bnagritech.tradesphere.retailer.repository.RetailerRepository;
import com.bnagritech.tradesphere.retailer.service.RetailerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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
                    .active(request.isActive())
                    .build();
        Retailer savedRetailer = retailerRepository.save(retailer);
        return mapToResponse(savedRetailer);
    }

    @Override
    public RetailerResponse getRetailerById(String retailerId) {
        return null;
    }

    @Override
    public RetailerResponse updateRetailer(String retailerId, RetailerRequest request) {
        return null;
    }

    @Override
    public RetailerResponse getRetailerByPhoneNumber(long phoneNumber) {
        return null;
    }

    @Override
    public RetailerResponse getRetailerByEmail(String email) {
        return null;
    }

    @Override
    public RetailerResponse updateRetailerStatus(String retailerId, RetailerRequest request) {
        return null;
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
        return List.of();
    }

    @Override
    public List<RetailerResponse> getRetailerByShopName(String shopName) {
        return List.of();
    }

    @Override
    public List<RetailerResponse> getRetailerByEmployeeId(String employeeId) {
        return List.of();
    }

    @Override
    public List<RetailerResponse> getRetailerByCity(String city) {
        return List.of();
    }

    @Override
    public List<RetailerResponse> getRetailerByState(String state) {
        return List.of();
    }

    @Override
    public List<RetailerResponse> getRetailerByStatus(Boolean active) {
        return List.of();
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
        retailerResponse.setActive(Boolean.parseBoolean(retailer.getAddress()));
        retailerResponse.setCreatedBy(retailer.getCreatedBy());
        retailerResponse.setUpdatedBy(retailer.getUpdatedBy());

        return retailerResponse;
    }
}
