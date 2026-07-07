package com.bnagritech.tradesphere.retailer.controller;


import com.bnagritech.tradesphere.promoter.dto.PromoterResponse;
import com.bnagritech.tradesphere.retailer.dto.RetailerRequest;
import com.bnagritech.tradesphere.retailer.dto.RetailerResponse;
import com.bnagritech.tradesphere.retailer.model.Retailer;
import com.bnagritech.tradesphere.retailer.service.RetailerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/retailers")
@RequiredArgsConstructor
public class RetailerController {
    private  final RetailerService retailerService;

    // create
    @PostMapping("/create")
    public ResponseEntity<RetailerResponse> createRetailer(
            @RequestBody RetailerRequest request){
        RetailerResponse response = retailerService.createRetailer(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    //get all retailers
    @GetMapping("/all")
    public ResponseEntity<List<RetailerResponse>>getAllRetailer() {
        return ResponseEntity.ok(retailerService.getAllRetailer());
    }
    //get retailer by id
    @GetMapping("/rtId/{retailerId}")
    public ResponseEntity<RetailerResponse> getRetailerById(@PathVariable String retailerId){
        return ResponseEntity.ok(retailerService.getRetailerById(retailerId));
    }
    //get retailer by phoneNumber
    @GetMapping("/phn/{phoneNumber}")
    public ResponseEntity<RetailerResponse> getRetailerByPhoneNumber(@PathVariable long phoneNumber){
        return ResponseEntity.ok(retailerService.getRetailerByPhoneNumber(phoneNumber));
    }
    //get retailers by city
    @GetMapping("/city/{city}")
    public ResponseEntity<List<RetailerResponse>> getRetailerByCity(@PathVariable String city){
        return ResponseEntity.ok(retailerService.getRetailerByCity(city));
    }
    //
}
