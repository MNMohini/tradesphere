package com.bnagritech.tradesphere.beat.controller;

import com.bnagritech.tradesphere.beat.dto.BeatsRequest;
import com.bnagritech.tradesphere.beat.dto.BeatsResponse;
import com.bnagritech.tradesphere.beat.service.BeatService;
import com.bnagritech.tradesphere.common.enums.BeatDay;
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
    public ResponseEntity<List<BeatsResponse>>getBeatsByTerritory(@PathVariable String territoryId) {
        return ResponseEntity.ok(beatService.getBeatsByTerritory(territoryId));
    }
    @GetMapping("emp/{employeeId}")
    public ResponseEntity<List<BeatsResponse>>getEmployeeBeats(@PathVariable String employeeId) {
        return ResponseEntity.ok(beatService.getEmployeeBeats(employeeId));
    }
    @GetMapping("/emp/{employeeId}/day/{beatDay}")
    public ResponseEntity<List<BeatsResponse>>getEmployeeDayBeats(@PathVariable String employeeId, @PathVariable BeatDay beatDay) {
        return ResponseEntity.ok(beatService.getEmployeeDayBeats(employeeId,beatDay));
    }
    
}
