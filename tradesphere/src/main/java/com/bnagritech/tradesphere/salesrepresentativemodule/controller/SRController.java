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

    @GetMapping("/id/{SRId}")
    public ResponseEntity<SRResponse> getSRById(@PathVariable String SRId) {
        return ResponseEntity.ok(srService.getSRById(SRId));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<SRResponse> getSRByEmail(@PathVariable String email) {
        return ResponseEntity.ok(srService.getSRByEmail(email));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/update/{SRId}")
    public ResponseEntity<SRResponse> updateSR(
            @PathVariable String SRId,
            @RequestBody SRRequest srRequest) {
        return ResponseEntity.ok(srService.updateSR(SRId, srRequest));
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

    @DeleteMapping("/del/{SRId}")
    public ResponseEntity<String> deleteSR(@PathVariable String SRId) {
        srService.deleteSR(SRId);
        return ResponseEntity.ok("Deleted successfully");
    }

}
