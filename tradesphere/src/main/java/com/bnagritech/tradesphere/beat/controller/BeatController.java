package com.bnagritech.tradesphere.beat.controller;

import com.bnagritech.tradesphere.auth.model.User;
import com.bnagritech.tradesphere.auth.dto.UserSummaryResponse;
import com.bnagritech.tradesphere.beat.dto.BeatAssignmentRequest;
import com.bnagritech.tradesphere.beat.dto.BeatsRequest;
import com.bnagritech.tradesphere.beat.dto.BeatsResponse;
import com.bnagritech.tradesphere.beat.service.BeatService;
import com.bnagritech.tradesphere.common.enums.BeatStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/beats")
@RequiredArgsConstructor
public class BeatController {
 private final BeatService beatService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<BeatsResponse>createBeat(
            @RequestBody @Valid BeatsRequest beatsRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(beatService.createBeat(beatsRequest));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/update/{beatId}")
    public ResponseEntity<BeatsResponse>updateBeat(
            @PathVariable String beatId,
            @RequestBody @Valid BeatsRequest beatsRequest) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(beatService.updateBeat(beatId, beatsRequest));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/id/{beatId}")
    public ResponseEntity<BeatsResponse>getBeatById(@PathVariable String beatId) {
        return ResponseEntity.ok(beatService.getBeatById(beatId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity <List<BeatsResponse>>getAllBeats(){
        return ResponseEntity.ok(beatService.getAllBeats());
    }

    @GetMapping("/my")
    public ResponseEntity<List<BeatsResponse>> getMyBeats(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(beatService.getMyBeats(currentUser));
    }

    @PreAuthorize("hasAnyRole('SALES_OFFICER', 'SUPERVISOR')")
    @GetMapping("/assignable")
    public ResponseEntity<List<BeatsResponse>> getAssignableBeats(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(beatService.getAssignableBeats(currentUser));
    }

    @PreAuthorize("hasAnyRole('SALES_OFFICER', 'SUPERVISOR')")
    @GetMapping("/assignable-users")
    public ResponseEntity<List<UserSummaryResponse>> getAssignableUsers(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(beatService.getAssignableUsers(currentUser));
    }

    @PreAuthorize("hasAnyRole('SALES_OFFICER', 'SUPERVISOR')")
    @PatchMapping("/assign/{beatId}")
    public ResponseEntity<BeatsResponse> assignBeat(
            @PathVariable String beatId,
            @RequestBody @Valid BeatAssignmentRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(beatService.assignBeat(beatId, request.getUserId(), currentUser));
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBeatById(String beatId) {
        beatService.deleteBeat(beatId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("territory/{territoryId}")
    public ResponseEntity<List<BeatsResponse>>getBeatsByTerritory(
            @PathVariable String territoryId) {
        return ResponseEntity.ok(beatService.getBeatsByTerritory(territoryId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/city/{city}")
    public ResponseEntity<List<BeatsResponse>>getAllBeatsByCity(
            @PathVariable String city) {
        return ResponseEntity.ok(beatService.searchByCity(city));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/state/{state}")
    public ResponseEntity<List<BeatsResponse>>getAllBeatsByState(
            @PathVariable String state){
        return ResponseEntity.ok(beatService.searchByState(state));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/find/{status}")
    public ResponseEntity<List<BeatsResponse>>getAllBeatsByStatus(
            @PathVariable BeatStatus status){
        return ResponseEntity.ok(beatService.getByBeatStatus(status));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("status/{beatId}")
    public ResponseEntity<BeatsResponse> updateBeatStatus(
            @PathVariable String beatId, @RequestBody BeatsRequest request) {
        return ResponseEntity.ok(beatService.updateBeatStatus(beatId, request));
    }

}
