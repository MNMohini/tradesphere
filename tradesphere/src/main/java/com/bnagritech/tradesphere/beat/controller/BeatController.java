package com.bnagritech.tradesphere.beat.controller;

import com.bnagritech.tradesphere.beat.dto.BeatsRequest;
import com.bnagritech.tradesphere.beat.dto.BeatsResponse;
import com.bnagritech.tradesphere.beat.service.BeatService;
import com.bnagritech.tradesphere.common.enums.ApprovalStatus;
import com.bnagritech.tradesphere.common.enums.BeatDay;
import com.bnagritech.tradesphere.common.enums.BeatStatus;
import com.bnagritech.tradesphere.common.enums.RetailerType;
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
    @GetMapping("emp/{employeeId}")
    public ResponseEntity<List<BeatsResponse>>getEmployeeBeats(
            @PathVariable String employeeId) {
        return ResponseEntity.ok(beatService.getEmployeeBeats(employeeId));
    }
    @GetMapping("/emp/{employeeId}/day/{beatDay}")
    public ResponseEntity<List<BeatsResponse>>getEmployeeDayBeats(
            @PathVariable String employeeId,
            @PathVariable BeatDay beatDay)
    {
        return ResponseEntity.ok(beatService.getEmployeeDayBeats(employeeId,beatDay));
    }
    @PostMapping("/id/{beatId}/retailers/{retailerId}")
    public ResponseEntity<BeatsResponse>assignRetailer(
            @PathVariable String beatId,
            @PathVariable String retailerId)
    {
        return ResponseEntity.ok(beatService.assignRetailerToBeat(beatId, retailerId));
    }
    @DeleteMapping("/id/{beatId}/retailers/{retailerId}")
    public ResponseEntity<BeatsResponse>deleteRetailer(
            @PathVariable String beatId,
            @PathVariable String retailerId)
    {
        return ResponseEntity.ok(beatService.removeRetailerFromBeat(beatId, retailerId));
    }
    @PutMapping("/id/{beatId}/approve/{managerId}")
    public ResponseEntity<BeatsResponse>approveBeat(
            @PathVariable String beatId,
            @PathVariable String managerId)
    {
        return ResponseEntity.ok(beatService.approveBeat(beatId, managerId));
    }
    @PutMapping("/id/{beatId}/reject/{managerId}")
    public ResponseEntity<BeatsResponse>rejectBeat(
            @PathVariable String beatId,
            @PathVariable String managerId)
    {
        return ResponseEntity.ok(beatService.rejectBeat(beatId, managerId));
    }
    @GetMapping("/city/{city}")
    public ResponseEntity<List<BeatsResponse>>getAllBeatsByCity(
            @PathVariable String city)
    {
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
        return ResponseEntity.ok(beatService.getByStatus(status));
    }
    @GetMapping("type/{beatType}")
    public ResponseEntity<List<BeatsResponse>>getAllBeatsByBeatType(
            @PathVariable RetailerType beatType    )
    {
        return ResponseEntity.ok(beatService.getByBeatType(beatType));
    }
    @GetMapping("/{approvalStatus}")
    public ResponseEntity<List<BeatsResponse>>getAllBeatsByApprovalStatus(
            @PathVariable ApprovalStatus approvalStatus)
    {
        return ResponseEntity.ok(beatService.getByApprovalStatus(approvalStatus));
    }
    @GetMapping("/{retailerId}")
    public ResponseEntity<BeatsResponse>getBeatByRetailer(
            @PathVariable String retailerId)
    {
        return ResponseEntity.ok(beatService.getBeatByRetailer(retailerId));
    }

}
