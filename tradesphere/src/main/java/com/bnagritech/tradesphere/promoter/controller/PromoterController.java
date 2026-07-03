package com.bnagritech.tradesphere.promoter.controller;
import com.bnagritech.tradesphere.employee.dto.EmployeeRequest;
import com.bnagritech.tradesphere.employee.dto.EmployeeResponse;
import com.bnagritech.tradesphere.promoter.dto.PromoterRequest;
import com.bnagritech.tradesphere.promoter.dto.PromoterResponse;
import com.bnagritech.tradesphere.promoter.repository.PromoterRepository;
import com.bnagritech.tradesphere.promoter.service.PromoterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/promoters")
@RequiredArgsConstructor
public class PromoterController {
    private final PromoterService promoterService;
     private final PromoterRepository promoterRepository;

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

    @DeleteMapping("/prmId/{promoterId}")
    public ResponseEntity<String> deletePromoter(@PathVariable String promoterId){
        promoterService.deletePromoter(promoterId);
        return ResponseEntity.ok("Promoter details deleted successfully");
    }

}
