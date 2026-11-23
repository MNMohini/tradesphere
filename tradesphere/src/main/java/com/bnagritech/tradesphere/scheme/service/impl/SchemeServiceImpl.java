package com.bnagritech.tradesphere.scheme.service.impl;

import com.bnagritech.tradesphere.scheme.dto.SchemeRequest;
import com.bnagritech.tradesphere.scheme.dto.SchemeResponse;

import com.bnagritech.tradesphere.scheme.model.Scheme;
import com.bnagritech.tradesphere.scheme.repository.SchemeRepository;
import com.bnagritech.tradesphere.scheme.service.SchemeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.bnagritech.tradesphere.common.enums.SchemeStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class SchemeServiceImpl implements SchemeService {

    private final SchemeRepository schemeRepository;

    @Override
    public SchemeResponse createScheme(
            SchemeRequest request,
            String currentUserId) {

        validateDates(request);

        String schemeCode = normalizeSchemeCode(
                request.getSchemeCode()
        );

        if (schemeRepository.existsBySchemeCode(schemeCode)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Scheme code already exists"
            );
        }

        Instant now = Instant.now();

        Scheme scheme = Scheme.builder()
                .schemeCode(schemeCode)
                .schemeName(request.getSchemeName().trim())
                .description(trimToNull(request.getDescription()))
                .schemeType(request.getSchemeType())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .productIds(request.getProductIds())
                .territoryIds(request.getTerritoryIds())
                .beatIds(request.getBeatIds())
                .outletIds(request.getOutletIds())
                .status(request.getStatus())
                .createdAt(now)
                .updatedAt(now)
                .createdBy(currentUserId)
                .updatedBy(currentUserId)
                .build();

        return toResponse(
                schemeRepository.save(scheme)
        );
    }

    @Override
    public SchemeResponse updateScheme(
            String schemeId,
            SchemeRequest request,
            String currentUserId) {

        Scheme scheme = getScheme(schemeId);

        validateDates(request);

        String schemeCode = normalizeSchemeCode(
                request.getSchemeCode()
        );

        if (schemeRepository.existsBySchemeCodeAndIdNot(
                schemeCode,
                schemeId
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Scheme code already exists"
            );
        }

        scheme.setSchemeCode(schemeCode);
        scheme.setSchemeName(request.getSchemeName().trim());
        scheme.setDescription(trimToNull(request.getDescription()));
        scheme.setSchemeType(request.getSchemeType());
        scheme.setStartDate(request.getStartDate());
        scheme.setEndDate(request.getEndDate());

        scheme.setProductIds(request.getProductIds());
        scheme.setTerritoryIds(request.getTerritoryIds());
        scheme.setBeatIds(request.getBeatIds());
        scheme.setOutletIds(request.getOutletIds());

        scheme.setStatus(request.getStatus());

        scheme.setUpdatedAt(Instant.now());
        scheme.setUpdatedBy(currentUserId);

        return toResponse(
                schemeRepository.save(scheme)
        );
    }

    @Override
    public SchemeResponse getSchemeById(String schemeId) {
        return toResponse(
                getScheme(schemeId)
        );
    }

    @Override
    public List<SchemeResponse> getAllSchemes() {
        return schemeRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<SchemeResponse> getSchemesByStatus(
            SchemeStatus status) {

        return schemeRepository.findByStatus(status)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<SchemeResponse> getActiveSchemes() {

        LocalDate today = LocalDate.now();

        return schemeRepository
                .findByStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        SchemeStatus.ACTIVE,
                        today,
                        today
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public void deleteScheme(String schemeId) {

        Scheme scheme = getScheme(schemeId);

        schemeRepository.delete(scheme);
    }

    private Scheme getScheme(String schemeId) {
        return schemeRepository.findById(schemeId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Scheme not found"
                        )
                );
    }

    private void validateDates(SchemeRequest request) {

        if (request.getStartDate() == null
                || request.getEndDate() == null) {
            return;
        }

        if (request.getEndDate()
                .isBefore(request.getStartDate())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "End date cannot be before start date"
            );
        }
    }

    private String normalizeSchemeCode(String schemeCode) {

        return schemeCode
                .trim()
                .toUpperCase(Locale.ROOT);
    }

    private String trimToNull(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private SchemeResponse toResponse(Scheme scheme) {

        return SchemeResponse.builder()
                .id(scheme.getId())
                .schemeCode(scheme.getSchemeCode())
                .schemeName(scheme.getSchemeName())
                .description(scheme.getDescription())
                .schemeType(scheme.getSchemeType())
                .startDate(scheme.getStartDate())
                .endDate(scheme.getEndDate())
                .productIds(scheme.getProductIds())
                .territoryIds(scheme.getTerritoryIds())
                .beatIds(scheme.getBeatIds())
                .outletIds(scheme.getOutletIds())
                .status(scheme.getStatus())
                .createdAt(scheme.getCreatedAt())
                .updatedAt(scheme.getUpdatedAt())
                .createdBy(scheme.getCreatedBy())
                .updatedBy(scheme.getUpdatedBy())
                .build();
    }
}