package com.bnagritech.tradesphere.retailer.service;

import com.bnagritech.tradesphere.retailer.dto.RetailerRequest;
import com.bnagritech.tradesphere.retailer.dto.RetailerResponse;

import java.util.List;

public interface RetailerService{

    RetailerResponse createRetailer(RetailerRequest request);
    RetailerResponse getRetailerByRetailerId(String retailerId);
    RetailerResponse updateRetailer(String retailerId,RetailerRequest request);
    RetailerResponse getRetailerByPhoneNumber(String phoneNumber);
    RetailerResponse updateRetailerStatus(String retailerId,RetailerRequest request);
    RetailerResponse updateOutletIds(String retailerId,RetailerRequest request);
    RetailerResponse getRetailerByRetailerName(String retailerName);
    List<RetailerResponse> getAllRetailer();
    void deleteRetailer (String retailerId );

}
