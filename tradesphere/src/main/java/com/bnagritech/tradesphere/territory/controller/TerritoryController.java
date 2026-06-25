package com.bnagritech.tradesphere.territory.controller;


import com.bnagritech.tradesphere.territory.dto.TerritoryRequest;
import com.bnagritech.tradesphere.territory.dto.TerritoryResponse;
import com.bnagritech.tradesphere.territory.repository.TerritoryRepository;
import com.bnagritech.tradesphere.territory.service.TerritoryService;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/territory")
public class TerritoryController {
    private final TerritoryService territoryService;

    @PostMapping
    public ResponseEntity<TerritoryResponse> createTerritory(@Valid @RequestBody TerritoryRequest request) {
        TerritoryResponse territoryResponse = territoryService.createTerritory(request);

        return new ResponseEntity<>(territoryResponse, HttpStatus.CREATED);
    }
    @GetMapping
    public ResponseEntity<List<TerritoryResponse>> getAllTerritories() {
        return ResponseEntity.ok(territoryService.getAllTerritories());
    }
    @GetMapping("/{id}")
    public ResponseEntity<TerritoryResponse> getTerritoryById(@PathVariable String id){
        return ResponseEntity.ok(territoryService.getTerritoryById(id));
    }
    @PutMapping("/{id}")
    public ResponseEntity<TerritoryResponse> updateTerritory(@PathVariable String id, @Valid @RequestBody TerritoryRequest request) {
        return ResponseEntity.ok(territoryService.updateTerritory(id, request));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTerritory(@PathVariable String id){
        territoryService.deleteTerritory(id);
        return ResponseEntity.ok("Territory deleted successfully");
    }


}
