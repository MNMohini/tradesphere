package com.bnagritech.tradesphere.stock;

import com.bnagritech.tradesphere.retailer.RetailerType;

import java.time.LocalDateTime;

public class Stock {
    private int id;
    private String retailerId;
    private String productId;
    private String availableQuantity;
    private LocalDateTime stockUpdateDate;
    private String updatedByEmployeeId;
    private String retailerFeedback;


}
