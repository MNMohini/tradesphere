package com.bnagritech.tradesphere.visit.service;

import com.bnagritech.tradesphere.auth.model.User;
import com.bnagritech.tradesphere.visit.dto.AddTrackPointsRequest;
import com.bnagritech.tradesphere.visit.dto.CancelVisitRequest;
import com.bnagritech.tradesphere.visit.dto.CheckInRequest;
import com.bnagritech.tradesphere.visit.dto.CheckOutRequest;
import com.bnagritech.tradesphere.visit.dto.EndVisitRequest;
import com.bnagritech.tradesphere.visit.dto.PlanVisitsRequest;
import com.bnagritech.tradesphere.visit.dto.PlanVisitsResponse;
import com.bnagritech.tradesphere.visit.dto.VisitResponse;
import com.bnagritech.tradesphere.visit.dto.VisitSearchRequest;
import com.bnagritech.tradesphere.visit.dto.VisitSummaryResponse;

import java.util.List;

/**
 * One visit (VI000000001) = one user's round of several outlets on one day.
 *
 *  plan       -> visit PLANNED, outlets PLANNED
 *  check-in   -> outlet IN_PROGRESS (first one also moves the visit to IN_PROGRESS)
 *  check-out  -> outlet COMPLETED (when no outlet is left, the visit becomes COMPLETED)
 *  end        -> visit COMPLETED now; outlets not visited become MISSED
 *  cancel     -> visit CANCELLED (an IN_PROGRESS visit only if it has no order)
 *  nightly    -> old PLANNED visits become MISSED, old IN_PROGRESS visits are closed
 *
 * Wherever a visitId is taken, both the tracking number (VI000000001) and the Mongo id work.
 */
public interface VisitService {

    PlanVisitsResponse planVisits(PlanVisitsRequest request, User currentUser);

    VisitResponse checkIn(CheckInRequest request, User currentUser);

    VisitResponse checkOut(String visitId, CheckOutRequest request, User currentUser);

    VisitResponse endVisit(String visitId, EndVisitRequest request, User currentUser);

    VisitResponse addTrackPoints(String visitId, AddTrackPointsRequest request, User currentUser);

    VisitResponse cancelVisit(String visitId, CancelVisitRequest request, User currentUser);

    VisitResponse getVisitById(String visitId, User currentUser);

    List<VisitSummaryResponse> searchVisits(VisitSearchRequest request, User currentUser);

    List<VisitSummaryResponse> getMyTodayVisits(User currentUser);

    List<VisitSummaryResponse> getActiveVisits(User currentUser);

    /** Nightly job. Returns how many visits were changed. */
    int markOverduePlannedVisitsAsMissed();
}
