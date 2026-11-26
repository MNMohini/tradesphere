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
 * validates it and hands it to VisitService; all rules live in the service.
 *
 * All endpoints need the JWT:  Authorization: Bearer <token>
 * {visitId} in a path = the Mongo "id" of the visit (not the readable VI-000001 code).
 *
 *  POST /api/visits/plan                    plan visits to several outlets for one day
 *  POST /api/visits/check-in                start a planned visit (visitId) or an unplanned one (outletCode)
 *  POST /api/visits/{visitId}/track         add GPS points while checked in
 *  POST /api/visits/{visitId}/check-out     end the visit with an outcome
 *  POST /api/visits/{visitId}/cancel        cancel a planned visit, or a wrong check-in without orders
 *  GET  /api/visits/my/today                my visits for today
 *  GET  /api/visits/active                  who is inside an outlet right now (own team)
 *  GET  /api/visits?status=&fromDate=&toDate=&outletCode=&userId=&beatId=   search
 *  GET  /api/visits/{visitId}               full details of one visit
 */
@RestController
@RequestMapping("/api/visits")
@RequiredArgsConstructor
public class VisitController {

    private final VisitService visitService;

    /**
     * Body: { "plannedDate": "2026-11-27", "outletCodes": ["OUT-001","OUT-002"], "beatId": "B01" }
     * 201 Created: one visit per outlet; skipped outlets are listed in skippedOutletIds.
     */
    @PostMapping("/plan")
    public ResponseEntity<PlanVisitsResponse> planVisits(
            @Valid @RequestBody PlanVisitsRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(visitService.planVisits(request, currentUser));
    }

    /**
     * Planned:   { "visitId": "<id>", "location": {"latitude": 26.85, "longitude": 80.95}, "photoFileId": "<optional>" }
     * Unplanned: { "outletCode": "OUT-003", "location": {...}, "clientVisitId": "<uuid>", "photoFileId": "<optional>" }
     */
    @PostMapping("/check-in")
    public ResponseEntity<VisitResponse> checkIn(
            @Valid @RequestBody CheckInRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(visitService.checkIn(request, currentUser));
    }

    /** Body: { "points": [ {"latitude": 26.85, "longitude": 80.95, "recordedAt": "2026-11-27T05:10:00Z"} ] } (max 50) */
    @PostMapping("/{visitId}/track")
    public ResponseEntity<VisitResponse> addTrackPoints(
            @PathVariable String visitId,
            @Valid @RequestBody AddTrackPointsRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(visitService.addTrackPoints(visitId, request, currentUser));
    }

    /** Body: { "location": {...}, "outcome": "ORDER_TAKEN", "remarks": "..." } (remarks needed for NO_ORDER, FOLLOW_UP_REQUIRED, OTHER) */
    @PostMapping("/{visitId}/check-out")
    public ResponseEntity<VisitResponse> checkOut(
            @PathVariable String visitId,
            @Valid @RequestBody CheckOutRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(visitService.checkOut(visitId, request, currentUser));
    }

    /** Body: { "reason": "Shop closed" } */
    @PostMapping("/{visitId}/cancel")
    public ResponseEntity<VisitResponse> cancelVisit(
            @PathVariable String visitId,
            @Valid @RequestBody CancelVisitRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(visitService.cancelVisit(visitId, request, currentUser));
    }

    @GetMapping("/my/today")
    public ResponseEntity<List<VisitSummaryResponse>> getMyTodayVisits(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(visitService.getMyTodayVisits(currentUser));
    }

    @GetMapping("/active")
    public ResponseEntity<List<VisitSummaryResponse>> getActiveVisits(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(visitService.getActiveVisits(currentUser));
    }

    /** e.g. /api/visits?status=COMPLETED&fromDate=2026-11-01&toDate=2026-11-07&outletCode=OUT-001 */
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
