package com.bnagritech.tradesphere.outlet.controller;


import com.bnagritech.tradesphere.common.enums.OutletStatus;
import com.bnagritech.tradesphere.common.enums.OutletType;
import com.bnagritech.tradesphere.outlet.dto.OutletRequest;
import com.bnagritech.tradesphere.outlet.dto.OutletResponse;
import com.bnagritech.tradesphere.outlet.service.OutletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/outlets")
@RequiredArgsConstructor
public class OutletController {
    private final OutletService outletService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/add")
    public ResponseEntity<OutletResponse> addOutlet(
            @RequestBody OutletRequest request){
        OutletResponse response = outletService.createOutlet(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    @GetMapping("/all")
    public ResponseEntity <List<OutletResponse>> getAll(){
        return ResponseEntity.ok(outletService.getAllOutlets());
    }
    @GetMapping("/id/{outletId}")
    public ResponseEntity<OutletResponse> getOutletByOutletId(@PathVariable String outletId){
        return ResponseEntity.ok(outletService.getOutletByOutletId(outletId));
    }
    @GetMapping("/phn/{phoneNumber}")
    public ResponseEntity<OutletResponse> getOutletByPhoneNumber(@PathVariable String phoneNumber) {
        return ResponseEntity.ok(outletService.getOutletByPhoneNumber(phoneNumber));
    }
    @GetMapping("/email/{email}")
    public ResponseEntity<OutletResponse> getOutletByEmail(@PathVariable String email) {
        return ResponseEntity.ok(outletService.getOutletByEmail(email));
    }
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/del/{outletId}")
    public ResponseEntity<String> deleteOutlet(@PathVariable String outletId) {
        outletService.deleteOutlet(outletId);
        return ResponseEntity.ok("Deleted successfully");
    }
    @GetMapping("/{promoterId}")
    public ResponseEntity<List<OutletResponse>> getOutletByPromoterId(@PathVariable String promoterId){
        return ResponseEntity.ok(outletService.getOutletsByPromoterId(promoterId));
    }
    @GetMapping("/list/{outletName}")
    public ResponseEntity<List<OutletResponse>> getOutletByOutletName(@PathVariable String outletName){
        return ResponseEntity.ok(outletService.getOutletsByOutletName(outletName));
    }
    @GetMapping("/tr/{territoryId}")
    public ResponseEntity<List<OutletResponse>> getOutletByTerritoryId(@PathVariable String territoryId){
        return ResponseEntity.ok(outletService.getOutletsByTerritoryId(territoryId));
    }
    @GetMapping("/city/{city}")
    public ResponseEntity<List<OutletResponse>> getOutletByCity(@PathVariable String city){
        return ResponseEntity.ok(outletService.getOutletByCity(city));
    }
    @GetMapping("/state/{state}")
    public ResponseEntity<List<OutletResponse>> getOutletByState(@PathVariable String state){
        return ResponseEntity.ok(outletService.getOutletByState(state));
    }
    @GetMapping("/type/{outletType}")
    public ResponseEntity<List<OutletResponse>> getOutletByOutletType(@PathVariable OutletType outletType){
        return ResponseEntity.ok(outletService.getOutletByOutletType(outletType));
    }
    @GetMapping("/status/{outletStatus}")
    public ResponseEntity<List<OutletResponse>> getOutletByOutletStatus(@PathVariable OutletStatus outletStatus){
        return ResponseEntity.ok(outletService.getOutletByOutletStatus(outletStatus));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/update/{outletId}")
    public ResponseEntity<OutletResponse> updateOutletStatus(@PathVariable String outletId,
                                                                 @RequestBody OutletRequest request){
        return ResponseEntity.ok(outletService.updateOutletStatus(outletId,request));
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{outletId}")
    public ResponseEntity<OutletResponse> updateOutlet(@PathVariable String outletId,
                                                           @RequestBody OutletRequest request) {
        return ResponseEntity.ok(outletService.updateOutlet(outletId, request));
    }
}

