package com.bnagritech.tradesphere.visit.service;

import com.bnagritech.tradesphere.visit.dto.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface VisitService {

    VisitResponse createVisit(VisitRequest request);
    VisitResponse updateVisit(String id, VisitRequest request);
    VisitResponse getVisitById(String id);
    List<VisitResponse> getAllVisits();
    List<VisitResponse> getVisitsByEmployee(String employeeId);
    List<VisitResponse> getVisitsByRetailer(String retailerId);
    VisitResponse checkIn(VisitCheckInRequest request);
    VisitResponse checkOut(VisitCheckOutResponse response);
    VisitResponse updateVisitStatus(String id, VisitStatusUpdateRequest request);
    void deleteVisit(String id);

}
