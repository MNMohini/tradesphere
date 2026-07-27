package com.bnagritech.tradesphere.visit.service.impl;

import com.bnagritech.tradesphere.common.exception.ResourceAlreadyExistsException;
import com.bnagritech.tradesphere.common.exception.ResourceNotFoundException;
import com.bnagritech.tradesphere.visit.dto.*;
import com.bnagritech.tradesphere.visit.model.Visit;
import com.bnagritech.tradesphere.visit.repository.VisitRepository;
import com.bnagritech.tradesphere.visit.service.VisitService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
@Service
@RequiredArgsConstructor
public class VisitServiceImpl implements VisitService {
    private final VisitRepository visitRepository;

    @Override
    public VisitResponse createVisit(VisitRequest request) {
        if(visitRepository.existsByVisitCode(request.getVisitCode()))
            throw new ResourceAlreadyExistsException("Visit code already exists");
        Visit visit = Visit.builder()
                .visitCode(request.getVisitCode())
                .remarks(request.getRemarks())
                .build();
        Visit savedVisit = visitRepository.save(visit);
        return mapToResponse(savedVisit);
    }

    @Override
    public VisitResponse updateVisit(String visitCode, VisitRequest request) {
        Visit visit = visitRepository.findByVisitCode(visitCode)
                .orElseThrow(()-> new ResourceNotFoundException("Visit code not found"));
        visit.setVisitCode(request.getVisitCode());
        visit.setRemarks(request.getRemarks());
        visit.setBeatId(request.getBeatId());
        visit.setVisitType(request.getVisitType());
        Visit updatedVisit = visitRepository.save(visit);
        return mapToResponse(updatedVisit);
    }

    @Override
    public VisitResponse getVisitById(String id) {
        return null;
    }

    @Override
    public List<VisitResponse> getAllVisits() {
        return List.of();
    }

    @Override
    public List<VisitResponse> getVisitsByEmployee(String employeeId) {
        return List.of();
    }

    @Override
    public List<VisitResponse> getVisitsByRetailer(String retailerId) {
        return List.of();
    }

    @Override
    public VisitResponse checkIn(VisitCheckInRequest request) {
        return null;
    }

    @Override
    public VisitResponse checkOut(VisitCheckOutResponse response) {
        return null;
    }

    @Override
    public VisitResponse updateVisitStatus(String id, VisitStatusUpdateRequest request) {
        return null;
    }

    @Override
    public void deleteVisit(String id) {

    }

    private VisitResponse mapToResponse(Visit visitRequest) {
        VisitResponse visitResponse = new VisitResponse();
        visitResponse.setVisitCode(visitRequest.getVisitCode());
        visitResponse.setRemarks(visitRequest.getRemarks());
        visitResponse.setBeatId(visitRequest.getBeatId());
        visitResponse.setCheckInTime(LocalDateTime.now());
        return visitResponse;
    }
}
