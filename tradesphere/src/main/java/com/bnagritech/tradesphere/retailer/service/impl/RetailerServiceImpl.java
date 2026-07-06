package com.bnagritech.tradesphere.retailer.service.impl;

import com.bnagritech.tradesphere.retailer.dto.RetailerRequest;
import com.bnagritech.tradesphere.retailer.dto.RetailerResponse;
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
            throw new RuntimeException("Resource already exists");
        }
        return null;
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
}
