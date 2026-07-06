package com.bnagritech.tradesphere.retailer.service;

import com.bnagritech.tradesphere.retailer.dto.RetailerRequest;
import com.bnagritech.tradesphere.retailer.dto.RetailerResponse;

import java.util.List;

public interface RetailerService{

    RetailerResponse createRetailer(RetailerRequest request);
    RetailerResponse getRetailerById(String retailerId);
    RetailerResponse updateRetailer(String retailerId,RetailerRequest request);
    RetailerResponse getRetailerByPhoneNumber(long phoneNumber);
    RetailerResponse getRetailerByEmail(String email);
    RetailerResponse updateRetailerStatus(String retailerId,RetailerRequest request);
    RetailerResponse assignRetailer(String retailerId,RetailerRequest request);
    void deleteRetailer (String retailerId );
    List<RetailerResponse> getRetailerByTerritory(String territoryId);
    List<RetailerResponse> getRetailerByShopName(String shopName);
    List<RetailerResponse> getRetailerByEmployeeId(String employeeId);
    List<RetailerResponse> getRetailerByCity(String city);
    List<RetailerResponse> getRetailerByState(String state);
    List<RetailerResponse> getRetailerByStatus(Boolean active);
    List<RetailerResponse> getAllRetailer();




}
