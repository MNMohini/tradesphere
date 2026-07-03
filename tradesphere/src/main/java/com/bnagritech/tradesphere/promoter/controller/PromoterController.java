package com.bnagritech.tradesphere.promoter.controller;

import com.bnagritech.tradesphere.employee.dto.EmployeeRequest;
import com.bnagritech.tradesphere.employee.dto.EmployeeResponse;
import com.bnagritech.tradesphere.promoter.dto.PromoterRequest;
import com.bnagritech.tradesphere.promoter.dto.PromoterResponse;
import com.bnagritech.tradesphere.promoter.repository.PromoterRepository;
import com.bnagritech.tradesphere.promoter.service.PromoterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
