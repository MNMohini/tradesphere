package com.bnagritech.tradesphere.beat.controller;

import com.bnagritech.tradesphere.beat.dto.BeatsRequest;
import com.bnagritech.tradesphere.beat.dto.BeatsResponse;
import com.bnagritech.tradesphere.beat.model.Beat;
import com.bnagritech.tradesphere.beat.service.BeatService;
import com.bnagritech.tradesphere.common.enums.ApprovalStatus;
import com.bnagritech.tradesphere.common.enums.BeatDay;
import com.bnagritech.tradesphere.common.enums.BeatStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/beats")
@RequiredArgsConstructor
public class BeatController {
 private final BeatService beatService;

    @PostMapping("/create")
    public ResponseEntity<BeatsResponse>createBeat(
            @RequestBody @Valid BeatsRequest beatsRequest) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(beatService.createBeat(beatsRequest));
    }
    @PutMapping("/update/{beatId}")
    public ResponseEntity<BeatsResponse>updateBeat(
            @PathVariable String beatId,
            @RequestBody @Valid BeatsRequest beatsRequest) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(beatService.updateBeat(beatId, beatsRequest));
    }
    @GetMapping("/id/{beatId}")
    public ResponseEntity<BeatsResponse>getBeatById(@PathVariable String beatId) {
        return ResponseEntity.ok(beatService.getBeatById(beatId));
    }
    @GetMapping("/all")
    public ResponseEntity <List<BeatsResponse>>getAllBeats(){
        return ResponseEntity.ok(beatService.getAllBeats());
    }
    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteBeatById(String beatId) {
        beatService.deleteBeat(beatId);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("territory/{territoryId}")
    public ResponseEntity<List<BeatsResponse>>getBeatsByTerritory(
            @PathVariable String territoryId) {
        return ResponseEntity.ok(beatService.getBeatsByTerritory(territoryId));
    }

    @GetMapping("/city/{city}")
    public ResponseEntity<List<BeatsResponse>>getAllBeatsByCity(
            @PathVariable String city) {
        return ResponseEntity.ok(beatService.searchByCity(city));
    }

    @GetMapping("/state/{state}")
    public ResponseEntity<List<BeatsResponse>>getAllBeatsByState(
            @PathVariable String state){
        return ResponseEntity.ok(beatService.searchByState(state));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<BeatsResponse>>getAllBeatsByStatus(
            @PathVariable BeatStatus status){
        return ResponseEntity.ok(beatService.getByBeatStatus(status));
    }

    @PatchMapping("/{beatId}/addoutlets/{outletId}")
    public ResponseEntity<BeatsResponse> addOutletToBeat(
            @PathVariable String beatId, @PathVariable String outletId) {
        return ResponseEntity.ok(beatService.addOutletToBeat(beatId, outletId));
    }
    @PatchMapping("/{beatId}/status")
    public ResponseEntity<BeatsResponse> updateBeatStatus(
            @PathVariable String beatId, @RequestParam BeatStatus beatStatus) {
        return ResponseEntity.ok(beatService.updateBeatStatus(beatId, beatStatus));
    }
    // Remove Outlet from Beat
    @DeleteMapping("/{beatId}/removeoutlets/{outletId}")
    public ResponseEntity<BeatsResponse> removeOutletFromBeat(
            @PathVariable String beatId, @PathVariable String outletId) {
        return ResponseEntity.ok(beatService.removeOutletFromBeat(beatId, outletId));
    }


}
