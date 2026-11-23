package com.bnagritech.tradesphere.scheme.controller;

import com.bnagritech.tradesphere.auth.model.User;
import com.bnagritech.tradesphere.scheme.dto.SchemeRequest;
import com.bnagritech.tradesphere.scheme.dto.SchemeResponse;
import com.bnagritech.tradesphere.common.enums.SchemeStatus;
import com.bnagritech.tradesphere.scheme.service.SchemeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schemes")
@RequiredArgsConstructor
public class SchemeController {

    private final SchemeService schemeService;

    @PostMapping("/create")
    public ResponseEntity<SchemeResponse> createScheme(
            @Valid @RequestBody SchemeRequest request,
            @AuthenticationPrincipal User currentUser) {

        SchemeResponse response = schemeService.createScheme(
                request,
                currentUser.getId()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/{schemeId}/update")
    public ResponseEntity<SchemeResponse> updateScheme(
            @PathVariable String schemeId,
            @Valid @RequestBody SchemeRequest request,
            @AuthenticationPrincipal User currentUser) {

        return ResponseEntity.ok(
                schemeService.updateScheme(
                        schemeId,
                        request,
                        currentUser.getId()
                )
        );
    }

    @GetMapping("/{schemeId}")
    public ResponseEntity<SchemeResponse> getSchemeById(
            @PathVariable String schemeId) {

        return ResponseEntity.ok(
                schemeService.getSchemeById(schemeId)
        );
    }

    @GetMapping
    public ResponseEntity<List<SchemeResponse>> getAllSchemes() {

        return ResponseEntity.ok(
                schemeService.getAllSchemes()
        );
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<SchemeResponse>> getSchemesByStatus(
            @PathVariable SchemeStatus status) {

        return ResponseEntity.ok(
                schemeService.getSchemesByStatus(status)
        );
    }

    @GetMapping("/active")
    public ResponseEntity<List<SchemeResponse>> getActiveSchemes() {

        return ResponseEntity.ok(
                schemeService.getActiveSchemes()
        );
    }

    @DeleteMapping("/del/{schemeId}")
    public ResponseEntity<Void> deleteScheme(
            @PathVariable String schemeId) {

        schemeService.deleteScheme(schemeId);

        return ResponseEntity.noContent().build();
    }
}