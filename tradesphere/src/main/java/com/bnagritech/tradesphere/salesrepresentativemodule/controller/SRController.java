package com.bnagritech.tradesphere.salesrepresentativemodule.controller;

import com.bnagritech.tradesphere.common.enums.UserStatus;
import com.bnagritech.tradesphere.salesrepresentativemodule.dto.SRRequest;
import com.bnagritech.tradesphere.salesrepresentativemodule.dto.SRResponse;
import com.bnagritech.tradesphere.salesrepresentativemodule.service.SRService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/SRs")
@RequiredArgsConstructor
public class SRController {
    private final SRService srService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<SRResponse> createSR(@RequestBody SRRequest srRequest) {
        return   ResponseEntity.ok(srService.createSR(srRequest));
    }

    @GetMapping("/all")
    public ResponseEntity<List<SRResponse>> getAllSR() {
        return ResponseEntity.ok(srService.getAllSRs());
    }

    @GetMapping("/id/{SrId}")
    public ResponseEntity<SRResponse> getSRById(@PathVariable String SrId) {
        return ResponseEntity.ok(srService.getSRById(SrId));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<SRResponse> getSRByEmail(@PathVariable String email) {
        return ResponseEntity.ok(srService.getSRByEmail(email));
    }

    @GetMapping("/ph/{phoneNumber}")
    public ResponseEntity<SRResponse> getSRByPhoneNumber(
            @PathVariable String phoneNumber) {
        return ResponseEntity.ok(srService.getSRByPhoneNumber(phoneNumber));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/update/{SrId}")
    public ResponseEntity<SRResponse> updateSR(
            @PathVariable String SrId,
            @RequestBody SRRequest srRequest) {
        return ResponseEntity.ok(srService.updateSR(SrId, srRequest));
    }

    @GetMapping("/territory/{territoryId}")
    public ResponseEntity<List<SRResponse>> getSRByTerritory(
            @PathVariable String territoryId) {
        return ResponseEntity.ok(srService.getAllSRsByTerritory(territoryId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<SRResponse>> getSRByStatus(
            @PathVariable UserStatus status) {
        return ResponseEntity.ok(srService.getAllSRsByStatus(status));
    }

    @DeleteMapping("/del/{SrId}")
    public ResponseEntity<String> deleteSR(@PathVariable String SrId) {
        srService.deleteSR(SrId);
        return ResponseEntity.ok("Deleted successfully");
    }

}
