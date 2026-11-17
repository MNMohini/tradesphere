package com.bnagritech.tradesphere.visit.controller;

import com.bnagritech.tradesphere.auth.model.User;
import com.bnagritech.tradesphere.visit.dto.AddTrackPointsRequest;
import com.bnagritech.tradesphere.visit.dto.CancelVisitRequest;
import com.bnagritech.tradesphere.visit.dto.CheckInRequest;
import com.bnagritech.tradesphere.visit.dto.CheckOutRequest;
import com.bnagritech.tradesphere.visit.dto.PlanVisitsRequest;
import com.bnagritech.tradesphere.visit.dto.PlanVisitsResponse;
import com.bnagritech.tradesphere.visit.dto.VisitResponse;
import com.bnagritech.tradesphere.visit.dto.VisitSearchRequest;
import com.bnagritech.tradesphere.visit.dto.VisitSummaryResponse;
import com.bnagritech.tradesphere.visit.service.VisitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST endpoints of the visit module. The controller only receives the request,
 * validates it and hands it to VisitService; the rules live in the service.
 */
@RestController
@RequestMapping("/api/visits")
@RequiredArgsConstructor
public class VisitController {

    private final VisitService visitService;

    // POST /api/visits/plan
    @PostMapping("/plan")
    public ResponseEntity<PlanVisitsResponse> planVisits(
            @Valid @RequestBody PlanVisitsRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED).body(visitService.planVisits(request, currentUser));
    }

    // POST /api/visits/check-in
    @PostMapping("/check-in")
    public ResponseEntity<VisitResponse> checkIn(
            @Valid @RequestBody CheckInRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(visitService.checkIn(request, currentUser));
    }

    // POST /api/visits/{visitId}/track
    @PostMapping("/{visitId}/track")
    public ResponseEntity<VisitResponse> addTrackPoints(
            @PathVariable String visitId,
            @Valid @RequestBody AddTrackPointsRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(visitService.addTrackPoints(visitId, request, currentUser));
    }

    // POST /api/visits/{visitId}/check-out
    @PostMapping("/{visitId}/check-out")
    public ResponseEntity<VisitResponse> checkOut(
            @PathVariable String visitId,
            @Valid @RequestBody CheckOutRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(visitService.checkOut(visitId, request, currentUser));
    }

    // POST /api/visits/{visitId}/cancel
    @PostMapping("/{visitId}/cancel")
    public ResponseEntity<VisitResponse> cancelVisit(
            @PathVariable String visitId,
            @Valid @RequestBody CancelVisitRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(visitService.cancelVisit(visitId, request, currentUser));
    }

    // GET /api/visits/my/today
    @GetMapping("/my/today")
    public ResponseEntity<List<VisitSummaryResponse>> getMyTodayVisits(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(visitService.getMyTodayVisits(currentUser));
    }

    // GET /api/visits/active   (who is inside an outlet right now)
    @GetMapping("/active")
    public ResponseEntity<List<VisitSummaryResponse>> getActiveVisits(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(visitService.getActiveVisits(currentUser));
    }

    // GET /api/visits?status=COMPLETED&fromDate=2026-11-01&toDate=2026-11-07
    @GetMapping
    public ResponseEntity<List<VisitSummaryResponse>> searchVisits(
            @Valid @ModelAttribute VisitSearchRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(visitService.searchVisits(request, currentUser));
    }

    // GET /api/visits/{visitId}
    @GetMapping("/{visitId}")
    public ResponseEntity<VisitResponse> getVisitById(
            @PathVariable String visitId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(visitService.getVisitById(visitId, currentUser));
    }
}
