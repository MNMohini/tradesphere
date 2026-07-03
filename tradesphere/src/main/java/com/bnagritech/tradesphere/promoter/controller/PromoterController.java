package com.bnagritech.tradesphere.promoter.controller;
import com.bnagritech.tradesphere.promoter.dto.PromoterRequest;
import com.bnagritech.tradesphere.promoter.dto.PromoterResponse;
import com.bnagritech.tradesphere.promoter.service.PromoterService;
import com.bnagritech.tradesphere.territory.dto.TerritoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/promoters")
@RequiredArgsConstructor
public class PromoterController {
    private final PromoterService promoterService;

    @PostMapping("/create")
    public ResponseEntity<PromoterResponse> createPromoter(@RequestBody PromoterRequest request){
        return ResponseEntity.ok(promoterService.createPromoter(request));
    }

    @GetMapping("/all")
    public ResponseEntity<List<PromoterResponse>>getAllPromoter() {
        return ResponseEntity.ok(promoterService.getAllPromoters());
    }

    @GetMapping("/prmId/{promoterId}")
    public ResponseEntity<PromoterResponse> getPromoterById(@PathVariable String promoterId){
        return ResponseEntity.ok(promoterService.getPromoterById(promoterId));
    }

    @PutMapping("/prmId{promoterId}")
    public ResponseEntity<PromoterResponse> updatePromoter(
            @PathVariable String promoterId,
            @RequestBody PromoterRequest request)
    {
        return ResponseEntity.ok(promoterService.updatePromoter(promoterId,request));
    }

    @GetMapping("/number/{phoneNumber}")
    public ResponseEntity<PromoterResponse> getPromoterByPhoneNumber(@PathVariable long phoneNumber){
        return ResponseEntity.ok(promoterService.getPromoterByPhoneNumber(phoneNumber));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<PromoterResponse> getPromoterByEmail(@PathVariable String email){
        return ResponseEntity.ok(promoterService.getPromoterByEmail(email));
    }

    @GetMapping("/territory/{territoryId}")
    public ResponseEntity<List<PromoterResponse>> getPromoterByTerritory(@PathVariable String territoryId) {
        return ResponseEntity.ok(promoterService.getPromoterByTerritory(territoryId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<PromoterResponse>> getPromoterByStatus(@PathVariable boolean status) {
        return ResponseEntity.ok(promoterService.getPromoterByStatus(status));
    }


    @DeleteMapping("/prmId/{promoterId}")
    public ResponseEntity<String> deletePromoter(@PathVariable String promoterId) {
        promoterService.deletePromoter(promoterId);
        return ResponseEntity.ok("Promoter details deleted successfully");
    }


}
