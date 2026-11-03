package com.bnagritech.tradesphere.visit.service;

import com.bnagritech.tradesphere.auth.model.User;
import com.bnagritech.tradesphere.visit.dto.AddTrackPointsRequest;
import com.bnagritech.tradesphere.visit.dto.CancelVisitRequest;
import com.bnagritech.tradesphere.visit.dto.CheckInRequest;
import com.bnagritech.tradesphere.visit.dto.CheckOutRequest;
import com.bnagritech.tradesphere.visit.dto.PlanVisitsRequest;
import com.bnagritech.tradesphere.visit.dto.PlanVisitsResponse;
import com.bnagritech.tradesphere.visit.dto.VisitResponse;
import com.bnagritech.tradesphere.visit.dto.VisitSearchRequest;
import com.bnagritech.tradesphere.visit.dto.VisitSummaryResponse;

import java.util.List;

/**
 * Lifecycle of a visit:  PLANNED -> IN_PROGRESS -> COMPLETED
 *                        PLANNED -> MISSED (nightly job) or CANCELLED
 *                        IN_PROGRESS -> CANCELLED (wrong check-in, only if no order was taken)
 * Every change is stored with who, when and where, so a visit can be traced end to end.
 */
public interface VisitService {

    /** Plans visits to several outlets on one day for the caller. */
    PlanVisitsResponse planVisits(PlanVisitsRequest request, User currentUser);

    /** Starts a planned visit, or an unplanned one. A retry with the same ids returns the same visit. */
    VisitResponse checkIn(CheckInRequest request, User currentUser);

    /** Adds GPS points while the visit is in progress. */
    VisitResponse addTrackPoints(String visitId, AddTrackPointsRequest request, User currentUser);

    /** Ends the visit with an outcome. */
    VisitResponse checkOut(String visitId, CheckOutRequest request, User currentUser);

    /** Cancels a planned visit, or a wrongly started one that has no order. */
    VisitResponse cancelVisit(String visitId, CancelVisitRequest request, User currentUser);

    /** Full details of one visit, if the caller is allowed to see it. */
    VisitResponse getVisitById(String visitId, User currentUser);

    /** Visits visible to the caller, narrowed by the filters. Defaults to the last 7 days. */
    List<VisitSummaryResponse> searchVisits(VisitSearchRequest request, User currentUser);

    /** The caller's own visits for today. */
    List<VisitSummaryResponse> getMyTodayVisits(User currentUser);

    /** Visits that are in progress right now among the people the caller can see (live view). */
    List<VisitSummaryResponse> getActiveVisits(User currentUser);

    /** Marks planned visits whose day has passed as MISSED. Returns how many were changed. */
    int markOverduePlannedVisitsAsMissed();
}