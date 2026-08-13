package com.bnagritech.tradesphere.retailer.controller;

import com.bnagritech.tradesphere.common.enums.RetailerStatus;
import com.bnagritech.tradesphere.retailer.dto.RetailerRequest;
import com.bnagritech.tradesphere.retailer.dto.RetailerResponse;
import com.bnagritech.tradesphere.retailer.service.RetailerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/retailers")
@RequiredArgsConstructor
public class RetailerController {
    private  final RetailerService retailerService;

    // create
    @PreAuthorize("hasRole('ADMIN')")
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
    public ResponseEntity<RetailerResponse> getRetailerByPhoneNumber(@PathVariable String phoneNumber){
        return ResponseEntity.ok(retailerService.getRetailerByPhoneNumber(phoneNumber));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{retailerId}")
    public ResponseEntity<RetailerResponse> updateRetailer(@PathVariable String retailerId,
                                                           @RequestBody RetailerRequest request){
        return ResponseEntity.ok(retailerService.updateRetailer(retailerId,request));
    }

    @PatchMapping("/status/{retailerId}")
    public ResponseEntity<RetailerResponse> updateRetailerStatus(@PathVariable String retailerId,
                                                           @RequestBody RetailerRequest request){
        return ResponseEntity.ok(retailerService.updateRetailerStatus(retailerId,request));
    }
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/del/{retailerId}")
    public ResponseEntity<String> deleteRetailer(@PathVariable String retailerId){
        retailerService.deleteRetailer(retailerId);
        return ResponseEntity.ok("Retailer deleted successfully");
    }

}
