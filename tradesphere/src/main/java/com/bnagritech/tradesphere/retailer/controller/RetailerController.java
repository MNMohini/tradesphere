package com.bnagritech.tradesphere.retailer.controller;

import com.bnagritech.tradesphere.common.enums.RetailerStatus;
import com.bnagritech.tradesphere.retailer.dto.RetailerRequest;
import com.bnagritech.tradesphere.retailer.dto.RetailerResponse;
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
    //get retailers by state
    @GetMapping("/state/{state}")
    public ResponseEntity<List<RetailerResponse>> getRetailerByState(@PathVariable String state){
        return ResponseEntity.ok(retailerService.getRetailerByState(state));
    }
    //By status
    @GetMapping("/status/{retailerStatus}")
    public ResponseEntity<List<RetailerResponse>> getRetailerByStatus(@PathVariable RetailerStatus retailerStatus){
        return ResponseEntity.ok(retailerService.getRetailerByRetailerStatus(retailerStatus));
    }
    //get retailer by employeeID
    @GetMapping("/empId/{employeeId}")
    public ResponseEntity<List<RetailerResponse>> getRetailerByEmployeeId(@PathVariable String employeeId){
        return ResponseEntity.ok(retailerService.getRetailerByEmployeeId(employeeId));
    }
    @GetMapping("/owner/{ownerName}")
    public ResponseEntity<List<RetailerResponse>> getRetailerByOwnerName(@PathVariable String ownerName){
        return ResponseEntity.ok(retailerService.getRetailerByOwnerName(ownerName));
    }
    @GetMapping("/shop/{shopName}")
    public ResponseEntity<List<RetailerResponse>> getRetailerByShopName(@PathVariable String shopName){
        return ResponseEntity.ok(retailerService.getRetailerByShopName(shopName));
    }
    @GetMapping("/territory/{territoryId}")
    public ResponseEntity<List<RetailerResponse>> getRetailerByTerritory(@PathVariable String territoryId){
        return ResponseEntity.ok(retailerService.getRetailerByTerritory(territoryId));
    }
    //by email
    @GetMapping("/email/{email}")
    public ResponseEntity<RetailerResponse> getRetailerByEmail(@PathVariable String email){
        return ResponseEntity.ok(retailerService.getRetailerByEmail(email));
    }
    @PutMapping("/{retailerId}")
    public ResponseEntity<RetailerResponse> updateRetailer(@PathVariable String retailerId,
                                                           @RequestBody RetailerRequest request){
        return ResponseEntity.ok(retailerService.updateRetailer(retailerId,request));
    }
    @PatchMapping("/assign/{retailerId}")
    public ResponseEntity<RetailerResponse> assignRetailer(@PathVariable String retailerId,
                                                           @RequestBody RetailerRequest request){
        return ResponseEntity.ok(retailerService.assignRetailer(retailerId,request));
    }
    @PatchMapping("/status/{retailerId}")
    public ResponseEntity<RetailerResponse> updateRetailerStatus(@PathVariable String retailerId,
                                                           @RequestBody RetailerRequest request){
        return ResponseEntity.ok(retailerService.updateRetailerStatus(retailerId,request));
    }
    @DeleteMapping("/del/{retailerId}")
    public ResponseEntity<String> deleteRetailer(@PathVariable String retailerId){
        retailerService.deleteRetailer(retailerId);
        return ResponseEntity.ok("Retailer deleted successfully");
    }

}
