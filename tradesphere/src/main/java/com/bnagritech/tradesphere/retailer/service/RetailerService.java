package com.bnagritech.tradesphere.retailer.service;

import com.bnagritech.tradesphere.auth.model.User;
import com.bnagritech.tradesphere.common.enums.RetailerStatus;
import com.bnagritech.tradesphere.retailer.dto.RetailerRequest;
import com.bnagritech.tradesphere.retailer.dto.RetailerResponse;

import java.util.List;

public interface RetailerService{

    RetailerResponse createRetailer(RetailerRequest request);
    RetailerResponse getRetailerById(String retailerId);
    RetailerResponse updateRetailer(String retailerId,RetailerRequest request);
    RetailerResponse getRetailerByPhoneNumber(String phoneNumber);
    RetailerResponse getRetailerByEmail(String email);
    RetailerResponse updateRetailerStatus(String retailerId,RetailerRequest request);
    RetailerResponse assignRetailer(String retailerId,RetailerRequest request);
    void deleteRetailer (String retailerId );
    List<RetailerResponse> getRetailerByTerritory(String territoryId);
    List<RetailerResponse> getRetailerByShopName(String shopName);
    List<RetailerResponse> getRetailerByOwnerName(String ownerName);
    List<RetailerResponse> getRetailerByUserName(String userName);
    List<RetailerResponse> getRetailerByCity(String city);
    List<RetailerResponse> getRetailerByState(String state);
    List<RetailerResponse> getRetailerByRetailerStatus(RetailerStatus status);
    List<RetailerResponse> getAllRetailer();

}
