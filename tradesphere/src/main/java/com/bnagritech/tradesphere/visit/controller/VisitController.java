package com.bnagritech.tradesphere.visit.controller;

import com.bnagritech.tradesphere.auth.model.User;
import com.bnagritech.tradesphere.visit.dto.AddTrackPointsRequest;
import com.bnagritech.tradesphere.visit.dto.CancelVisitRequest;
import com.bnagritech.tradesphere.visit.dto.CheckInRequest;
import com.bnagritech.tradesphere.visit.dto.CheckOutRequest;
import com.bnagritech.tradesphere.visit.dto.EndVisitRequest;
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
 * Visit endpoints. All need:  Authorization: Bearer <token>
 * {visitId} in a path accepts the tracking number (VI000000001) or the Mongo id.
 *
 *  POST /api/visits/plan                  plan one visit with several outlets for a day
 *  POST /api/visits/check-in              check in at one outlet
 *  POST /api/visits/{visitId}/check-out   check out from the current outlet
 *  POST /api/visits/{visitId}/end         end the whole visit (outlets not visited -> MISSED)
 *  POST /api/visits/{visitId}/track       add GPS points while the visit is in progress
 *  POST /api/visits/{visitId}/cancel      cancel the visit
 *  GET  /api/visits/my/today              my visits today
 *  GET  /api/visits/active                visits in progress now (own team)
 *  GET  /api/visits?status=&fromDate=&toDate=&outletCode=&userId=&beatId=
 *  GET  /api/visits/{visitId}             full details (track a visit by VI000000001)
 */
@RestController
@RequestMapping("/api/visits")
@RequiredArgsConstructor
public class VisitController {

    private final VisitService visitService;

    /** { "plannedDate": "2026-11-27", "outletCodes": ["OUT-001", "OUT-002"], "beatId": "B01" } */
    @PostMapping("/plan")
    public ResponseEntity<PlanVisitsResponse> planVisits(
            @Valid @RequestBody PlanVisitsRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED).body(visitService.planVisits(request, currentUser));
    }

    /** { "visitId": "VI000000001", "outletCode": "OUT-001", "location": {"latitude": 26.85, "longitude": 80.95}, "photoFileId": "..." } */
    @PostMapping("/check-in")
    public ResponseEntity<VisitResponse> checkIn(
            @Valid @RequestBody CheckInRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(visitService.checkIn(request, currentUser));
    }

    /** { "location": {...}, "outcome": "ORDER_TAKEN", "remarks": "..." } */
    @PostMapping("/{visitId}/check-out")
    public ResponseEntity<VisitResponse> checkOut(
            @PathVariable String visitId,
            @Valid @RequestBody CheckOutRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(visitService.checkOut(visitId, request, currentUser));
    }

    /** { "location": {...}, "remarks": "Rain, could not cover the rest" } */
    @PostMapping("/{visitId}/end")
    public ResponseEntity<VisitResponse> endVisit(
            @PathVariable String visitId,
            @Valid @RequestBody EndVisitRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(visitService.endVisit(visitId, request, currentUser));
    }

    /** { "points": [ {"latitude": 26.85, "longitude": 80.95, "recordedAt": "2026-11-27T05:10:00Z"} ] } */
    @PostMapping("/{visitId}/track")
    public ResponseEntity<VisitResponse> addTrackPoints(
            @PathVariable String visitId,
            @Valid @RequestBody AddTrackPointsRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(visitService.addTrackPoints(visitId, request, currentUser));
    }

    /** { "reason": "Beat changed" } */
    @PostMapping("/{visitId}/cancel")
    public ResponseEntity<VisitResponse> cancelVisit(
            @PathVariable String visitId,
            @Valid @RequestBody CancelVisitRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(visitService.cancelVisit(visitId, request, currentUser));
    }

    @GetMapping("/my/today")
    public ResponseEntity<List<VisitSummaryResponse>> getMyTodayVisits(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(visitService.getMyTodayVisits(currentUser));
    }

    @GetMapping("/active")
    public ResponseEntity<List<VisitSummaryResponse>> getActiveVisits(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(visitService.getActiveVisits(currentUser));
    }

    @GetMapping
    public ResponseEntity<List<VisitSummaryResponse>> searchVisits(
            @Valid @ModelAttribute VisitSearchRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(visitService.searchVisits(request, currentUser));
    }

    @GetMapping("/{visitId}")
    public ResponseEntity<VisitResponse> getVisitById(
            @PathVariable String visitId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(visitService.getVisitById(visitId, currentUser));
    }
}
