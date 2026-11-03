package com.bnagritech.tradesphere.outlet.controller;

import com.bnagritech.tradesphere.auth.model.User;
import com.bnagritech.tradesphere.outlet.dto.*;
import com.bnagritech.tradesphere.outlet.service.OutletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST endpoints of the outlet module. The controller only receives the request,
 * validates it and hands it to OutletService; the rules live in the service.
 */
@RestController
@RequestMapping("/api/outlets")
@RequiredArgsConstructor
public class OutletController {

    private final OutletService outletService;

    // POST /api/outlets
    @PostMapping
    public ResponseEntity<OutletResponse> createOutlet(
            @Valid @RequestBody CreateOutletRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED).body(outletService.createOutlet(request, currentUser));
    }

    // GET /api/outlets?keyword=Sharma&city=Delhi&status=ACTIVE&limit=50
    @GetMapping
    public ResponseEntity<List<OutletSummaryResponse>> searchOutlets(
            @Valid @ModelAttribute OutletSearchRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(outletService.searchOutlets(request, currentUser));
    }

    // GET /api/outlets/nearby?latitude=28.6139&longitude=77.2090&radiusMeters=500
    @GetMapping("/nearby")
    public ResponseEntity<List<NearByOutletResponse>> findNearbyOutlets(
            @Valid @ModelAttribute NearByOutletRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(outletService.findNearbyOutlets(request, currentUser));
    }

    // GET /api/outlets/beat/{beatId}
    @GetMapping("/beat/{beatId}")
    public ResponseEntity<List<OutletSummaryResponse>> getOutletsByBeat(
            @PathVariable String beatId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(outletService.getOutletsByBeat(beatId, currentUser));
    }

    // GET /api/outlets/code/{outletCode}
    @GetMapping("/code/{outletCode}")
    public ResponseEntity<OutletResponse> getOutletByCode(
            @PathVariable String outletCode,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(outletService.getOutletByCode(outletCode, currentUser));
    }

    // GET /api/outlets/{outletId}
    @GetMapping("/{outletId}")
    public ResponseEntity<OutletResponse> getOutletById(
            @PathVariable String outletId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(outletService.getOutletById(outletId, currentUser));
    }

    // PUT /api/outlets/{outletId}
    @PutMapping("/{outletId}")
    public ResponseEntity<OutletResponse> updateOutlet(
            @PathVariable String outletId,
            @Valid @RequestBody UpdateOutletRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(outletService.updateOutlet(outletId, request, currentUser));
    }

    // PATCH /api/outlets/{outletId}/status
    @PatchMapping("/{outletId}/status")
    public ResponseEntity<OutletResponse> updateOutletStatus(
            @PathVariable String outletId,
            @Valid @RequestBody UpdateOutletStatusRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(outletService.updateOutletStatus(outletId, request, currentUser));
    }
}
