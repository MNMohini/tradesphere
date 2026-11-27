package com.bnagritech.tradesphere.visit.service.impl;

import com.bnagritech.tradesphere.auth.model.User;
import com.bnagritech.tradesphere.auth.repository.UserRepository;
import com.bnagritech.tradesphere.beat.model.Beat;
import com.bnagritech.tradesphere.beat.repository.BeatRepository;
import com.bnagritech.tradesphere.common.enums.*;
import com.bnagritech.tradesphere.common.exception.ResourceNotFoundException;
import com.bnagritech.tradesphere.common.util.GeoUtils;
import com.bnagritech.tradesphere.common.util.SequenceGenerator;
import com.bnagritech.tradesphere.imagekit.dto.MediaFolder;
import com.bnagritech.tradesphere.imagekit.dto.MediaRef;
import com.bnagritech.tradesphere.imagekit.service.ImageKitService;
import com.bnagritech.tradesphere.order.repository.OrderRepository;
import com.bnagritech.tradesphere.outlet.model.OutletAddress;
import com.bnagritech.tradesphere.outlet.repository.OutletRepository;
import com.bnagritech.tradesphere.visit.dto.AddTrackPointsRequest;
import com.bnagritech.tradesphere.visit.dto.CancelVisitRequest;
import com.bnagritech.tradesphere.visit.dto.CheckInRequest;
import com.bnagritech.tradesphere.visit.dto.CheckOutRequest;
import com.bnagritech.tradesphere.visit.dto.EndVisitRequest;
import com.bnagritech.tradesphere.visit.dto.GeoPointDto;
import com.bnagritech.tradesphere.visit.dto.PlanVisitsRequest;
import com.bnagritech.tradesphere.visit.dto.PlanVisitsResponse;
import com.bnagritech.tradesphere.visit.dto.PunchResponse;
import com.bnagritech.tradesphere.visit.dto.TrackPointDto;
import com.bnagritech.tradesphere.visit.dto.VisitOutletResponse;
import com.bnagritech.tradesphere.visit.dto.VisitOutletSummaryResponse;
import com.bnagritech.tradesphere.visit.dto.VisitResponse;
import com.bnagritech.tradesphere.visit.dto.VisitSearchRequest;
import com.bnagritech.tradesphere.visit.dto.VisitSummaryResponse;
import com.bnagritech.tradesphere.visit.dto.VisitorAttendanceItemResponse;
import com.bnagritech.tradesphere.visit.dto.VisitorAttendancePageResponse;
import com.bnagritech.tradesphere.visit.dto.VisitorAttendanceSearchRequest;
import com.bnagritech.tradesphere.visit.model.TrackPoint;
import com.bnagritech.tradesphere.visit.model.Visit;
import com.bnagritech.tradesphere.visit.model.VisitOutlet;
import com.bnagritech.tradesphere.visit.model.VisitPunch;
import com.bnagritech.tradesphere.visit.model.VisitStatusHistory;
import com.bnagritech.tradesphere.visit.repository.VisitFilter;
import com.bnagritech.tradesphere.visit.repository.VisitRepository;
import com.bnagritech.tradesphere.visit.service.VisitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class VisitServiceImpl implements VisitService {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");
    private static final Duration MAX_CLOCK_SKEW = Duration.ofMinutes(10);
    private static final Duration MAX_OFFLINE_AGE = Duration.ofDays(3);
    private static final int MAX_TRACK_POINTS = 500;
    private static final int MAX_PLAN_DAYS_AHEAD = 30;
    private static final int DEFAULT_SEARCH_DAYS = 7;
    private static final int MAX_OUTLETS_PER_PLAN = 20;

    // Field roles that make visits
    private static final Set<UserRole> VISIT_ROLES = EnumSet.of(
            UserRole.SALES_REPRESENTATIVE, UserRole.SALES_OFFICER, UserRole.SUPERVISOR, UserRole.PROMOTER);

    // Roles that may see every visit
    private static final Set<UserRole> SEE_ALL_ROLES = EnumSet.of(UserRole.ADMIN, UserRole.NATIONAL_HEAD);

    // Outcomes that need an explanation
    private static final Set<VisitOutcome> REMARKS_REQUIRED = EnumSet.of(
            VisitOutcome.NO_ORDER, VisitOutcome.FOLLOW_UP_REQUIRED, VisitOutcome.OTHER);

    // Visit summary outcome: the first of these found among the outlets wins
    private static final List<VisitOutcome> OUTCOME_PRIORITY = List.of(
            VisitOutcome.ORDER_TAKEN, VisitOutcome.FOLLOW_UP_REQUIRED, VisitOutcome.NO_ORDER, VisitOutcome.OTHER);

    // Order statuses that do not count as a live order for a visit
    private static final Set<OrderStatus> DEAD_ORDER_STATUSES = EnumSet.of(OrderStatus.CANCELLED, OrderStatus.RETURNED);

    // Visit days that are still "open" (outlets can be added / visited)
    private static final Set<VisitStatus> OPEN = EnumSet.of(VisitStatus.PLANNED, VisitStatus.IN_PROGRESS);

    // An outlet in one of these statuses is already taken care of for that day
    private static final Set<VisitStatus> OUTLET_TAKEN = EnumSet.of(
            VisitStatus.PLANNED, VisitStatus.IN_PROGRESS, VisitStatus.COMPLETED);

    // Allowed visit status changes. Anything not listed is rejected.
    private static final Map<VisitStatus, Set<VisitStatus>> TRANSITIONS = Map.of(
            VisitStatus.PLANNED, EnumSet.of(VisitStatus.IN_PROGRESS, VisitStatus.CANCELLED, VisitStatus.MISSED),
            VisitStatus.IN_PROGRESS, EnumSet.of(VisitStatus.COMPLETED, VisitStatus.CANCELLED),
            VisitStatus.COMPLETED, EnumSet.noneOf(VisitStatus.class),
            VisitStatus.MISSED, EnumSet.noneOf(VisitStatus.class),
            VisitStatus.CANCELLED, EnumSet.noneOf(VisitStatus.class)
    );

    private final VisitRepository visitRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OutletRepository outletRepository;
    private final BeatRepository beatRepository;
    private final ImageKitService imageKitService;
    private final SequenceGenerator sequenceGenerator;

    // A check-in without a photo is rejected.
    @Value("${app.visit.require-checkin-photo:true}")
    private boolean requireCheckInPhoto = true;

    @Value("${app.visit.checkin-radius-meters:100}")
    private double checkInRadiusMeters = 100.0;

    // Punches outside the outlet radius are rejected.
    @Value("${app.visit.enforce-geofence:true}")
    private boolean enforceGeofence = true;

    // =====================================================================
    // Plan
    // =====================================================================

    @Override
    public PlanVisitsResponse planVisits(PlanVisitsRequest request, User currentUser) {
        requireVisitRole(currentUser);

        LocalDate today = LocalDate.now(IST);
        LocalDate day = request.getPlannedDate();
        if (day.isBefore(today)) {
            throw badRequest("plannedDate cannot be in the past");
        }
        if (day.isAfter(today.plusDays(MAX_PLAN_DAYS_AHEAD))) {
            throw badRequest("You can plan at most " + MAX_PLAN_DAYS_AHEAD + " days ahead");
        }
        String beatId = blankToNull(request.getBeatId());
        if (beatId == null) {
            throw badRequest("beatId is required");
        }
        Beat beat = beatRepository.findByBeatId(beatId)
                .orElseThrow(() -> new ResourceNotFoundException("Beat not found: " + beatId));
        boolean assignedBeat = currentUser.getId().equals(beat.getAssignedUserId())
                || (beat.getAssignedUserId() == null
                && currentUser.getUsername().equalsIgnoreCase(beat.getUserName()));
        if (!assignedBeat) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This beat is not assigned to you");
        }

        // Clean the list: drop blanks and repeats, keep the order sent
        Set<String> outletCodes = new LinkedHashSet<>();
        if (request.getOutletCodes() != null) {
            for (String code : request.getOutletCodes()) {
                String clean = blankToNull(code);
                if (clean != null) {
                    outletCodes.add(clean);
                }
            }
        }
        if (outletCodes.isEmpty()) {
            throw badRequest("outletCodes must contain at least one outlet");
        }
        if (outletCodes.size() > MAX_OUTLETS_PER_PLAN) {
            throw badRequest("You can plan at most " + MAX_OUTLETS_PER_PLAN + " outlets at a time");
        }

        String visitDay = day.toString();
        Instant now = Instant.now();

        // An outlet already planned or visited that day cannot be planned again.
        List<Visit> sameDay = visitRepository.findByUserIdAndVisitDayAndStatusIn(
                currentUser.getId(), visitDay, EnumSet.of(VisitStatus.PLANNED, VisitStatus.IN_PROGRESS, VisitStatus.COMPLETED));
        Set<String> alreadyTaken = new HashSet<>();
        for (Visit v : sameDay) {
            if (v.getOutletVisits() != null && !v.getOutletVisits().isEmpty()) {
                for (VisitOutlet outlet : v.getOutletVisits()) {
                    if (OUTLET_TAKEN.contains(outlet.getStatus())) {
                        alreadyTaken.add(outlet.getOutletCode());
                    }
                }
            } else if (OUTLET_TAKEN.contains(v.getStatus()) && v.getOutletCodes() != null) {
                alreadyTaken.addAll(v.getOutletCodes());
            }
        }

        Map<String, OutletInfo> toAdd = new LinkedHashMap<>();
        List<String> skipped = new ArrayList<>();
        for (String outletReference : outletCodes) {
            Optional<OutletInfo> outlet = findOutlet(outletReference);
            if (outlet.isEmpty() || !outlet.get().active()) {
                skipped.add(outletReference);
                continue;
            }
            if (!isOutletInBeat(outlet.get(), outletReference, beat)) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "Outlet " + outletReference + " is not part of beat " + beatId);
            }
            if (alreadyTaken.contains(outlet.get().outletCode())) {
                skipped.add(outletReference);
            } else {
                toAdd.put(outlet.get().outletCode(), outlet.get());
            }
        }

        if (toAdd.isEmpty()) {
            return PlanVisitsResponse.builder()
                    .requested(outletCodes.size())
                    .created(0)
                    .visits(List.of())
                    .skippedOutletIds(skipped)
                    .build();
        }

        List<Visit> createdVisits = new ArrayList<>();
        for (Map.Entry<String, OutletInfo> outletEntry : toAdd.entrySet()) {
            Visit visit = newVisit(currentUser, visitDay, true, request.getBeatId(), now, "Visit planned");
            addOutlet(visit, outletEntry.getKey(), true, request.getBeatId(), outletEntry.getValue());
            visit.setUpdatedBy(currentUser.getId());
            try {
                createdVisits.add(visitRepository.save(visit));
            } catch (DuplicateKeyException e) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "A selected outlet was just planned. Refresh your visits and try again.");
            }
        }

        return PlanVisitsResponse.builder()
                .visitId(createdVisits.get(0).getVisitId())
                .requested(outletCodes.size())
                .created(toAdd.size())
                .visits(createdVisits.stream().map(this::toSummary).toList())
                .skippedOutletIds(skipped)
                .build();
    }

    // =====================================================================
    // Check-in at one outlet
    // =====================================================================

    @Override
    public VisitResponse checkIn(CheckInRequest request, User currentUser) {
        requireVisitRole(currentUser);

        String outletReference = blankToNull(request.getOutletCode());
        if (outletReference == null) {
            throw badRequest("outletCode is required");
        }
        OutletInfo outlet = findOutlet(outletReference)
                .orElseThrow(() -> badRequest("Outlet not found: " + outletReference));
        if (!outlet.active()) {
            throw badRequest("Outlet is not active: " + outletReference);
        }
        String outletCode = outlet.outletCode();
        String clientVisitId = blankToNull(request.getClientVisitId());

        Instant now = Instant.now();
        Instant checkInTime = resolveDeviceTime(request.getCheckInTime(), now);
        LocalDate checkInDay = checkInTime.atZone(IST).toLocalDate();

        // 1. Which visit?
        Visit visit = findTargetVisit(request, clientVisitId, checkInDay, currentUser, outletCode);
        if (visit == null) {
            // No open visit today: a walk-in starts a new unplanned visit
            visit = newVisit(currentUser, checkInDay.toString(), false, request.getBeatId(), now,
                    "Visit started without a plan");
            visit.setClientVisitId(clientVisitId);
        }

        // 2. Only one visit in progress per user
        Optional<Visit> active = visitRepository.findFirstByUserIdAndStatus(currentUser.getId(), VisitStatus.IN_PROGRESS);
        if (active.isPresent() && !active.get().getId().equals(visit.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Visit " + active.get().getId() + " is still in progress. End it first.");
        }

        // 3. Which outlet inside the visit?
        VisitOutlet current = currentOutlet(visit);
        VisitOutlet target = outletOf(visit, outletCode);
        if (target != null && target.getStatus() == VisitStatus.IN_PROGRESS) {
            return toResponse(visit);   // already checked in here: this is a retry
        }
        if (current != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "You are checked in at " + current.getOutletCode() + ". Check out there first.");
        }
        if (target != null && target.getStatus() != VisitStatus.PLANNED) {
            throw badRequest("Outlet " + outletCode + " is already " + target.getStatus() + " in this visit");
        }
        if (target == null && visit.getOutletCodes() != null && !visit.getOutletCodes().isEmpty()) {
            throw badRequest("Visit " + visit.getVisitId() + " belongs to a different outlet");
        }

        if (target == null) {
            target = addOutlet(visit, outletCode, false, request.getBeatId(), outlet);
        }

        // 4. The outlet must have a saved location so the server can enforce its radius.
        requireOutletLocation(outlet.location());
        VisitPunch punch = buildPunch(checkInTime, request.getLocation(), outlet.location());
        requireWithinOutletRadius(punch, "check in");

        // 5. Photo with the outlet (uploaded to ImageKit first; the app sends only the fileId)
        String photoFileId = blankToNull(request.getPhotoFileId());
        if (photoFileId == null && requireCheckInPhoto) {
            throw badRequest("A photo with the outlet is required to check in");
        }
        if (photoFileId != null) {
            target.setPictureWithOutlet(loadCheckInPhoto(photoFileId, currentUser, now));
        }

        // 6. Apply
        target.setCheckIn(punch);
        target.setStatus(VisitStatus.IN_PROGRESS);
        if (visit.getStatus() == VisitStatus.PLANNED) {
            visit.setCheckIn(punch);   // the visit starts at its first outlet
            changeStatus(visit, VisitStatus.IN_PROGRESS, currentUser.getId(), "Checked in at " + outletCode, now);
        }
        if (visit.getBeatId() == null && request.getBeatId() != null) {
            visit.setBeatId(request.getBeatId());
        }
        visit.setUpdatedBy(currentUser.getId());

        try {
            return toResponse(visitRepository.save(visit));
        } catch (DuplicateKeyException e) {
            // A retried walk-in with the same clientVisitId was saved a moment earlier
            if (clientVisitId != null) {
                Optional<Visit> winner = visitRepository.findByClientVisitId(clientVisitId);
                if (winner.isPresent() && isOwner(winner.get(), currentUser)) {
                    return toResponse(winner.get());
                }
            }
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Another visit is already in progress. Refresh and try again.");
        }
    }

    /** visitId given -> that visit; retry -> its visit; otherwise today's open visit containing the outlet. */
    private Visit findTargetVisit(
            CheckInRequest request,
            String clientVisitId,
            LocalDate checkInDay,
            User user,
            String outletCode) {
        String visitKey = blankToNull(request.getVisitId());
        if (visitKey != null) {
            Visit visit = findVisit(visitKey)
                    .orElseThrow(() -> new ResourceNotFoundException("Visit not found: " + visitKey));
            if (!isOwner(visit, user)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This visit belongs to another user");
            }
            if (!OPEN.contains(visit.getStatus())) {
                throw badRequest("Visit " + visit.getVisitId() + " is " + visit.getStatus());
            }
            if (LocalDate.parse(visit.getVisitDay()).isAfter(checkInDay)) {
                throw badRequest("Visit " + visit.getVisitId() + " is planned for " + visit.getVisitDay());
            }
            return visit;
        }

        if (clientVisitId != null) {
            Optional<Visit> retried = visitRepository.findByClientVisitId(clientVisitId);
            if (retried.isPresent()) {
                if (!isOwner(retried.get(), user)) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "clientVisitId is already used by another user");
                }
                return retried.get();
            }
        }

        // Today's open visit: the one in progress first, then the planned one
        List<Visit> open = visitRepository.findByUserIdAndVisitDayAndStatusIn(user.getId(), checkInDay.toString(), OPEN);
        return open.stream()
                .filter(v -> outletOf(v, outletCode) != null)
                .min(Comparator.comparing(v -> v.getStatus() == VisitStatus.IN_PROGRESS ? 0 : 1))
                .orElse(null);
    }

    // =====================================================================
    // Check-out from the current outlet
    // =====================================================================

    @Override
    public VisitResponse checkOut(String visitId, CheckOutRequest request, User currentUser) {
        Visit visit = loadOwnedVisit(visitId, currentUser);
        if (visit.getStatus() != VisitStatus.IN_PROGRESS) {
            throw badRequest("Visit " + visit.getVisitId() + " is " + visit.getStatus());
        }
        VisitOutlet current = currentOutlet(visit);
        if (current == null || current.getCheckIn() == null) {
            throw badRequest("You are not checked in at any outlet of this visit");
        }

        Instant now = Instant.now();
        Instant checkOutTime = resolveDeviceTime(request.getCheckOutTime(), now);
        if (checkOutTime.isBefore(current.getCheckIn().getTime())) {
            throw badRequest("checkOutTime is before the check-in time");
        }

        VisitOutcome outcome = request.getOutcome();
        String remarks = blankToNull(request.getRemarks());
        if (REMARKS_REQUIRED.contains(outcome) && remarks == null) {
            throw badRequest("remarks are required for outcome " + outcome);
        }

        GeoJsonPoint outletLocation = findOutlet(current.getOutletCode())
                .map(OutletInfo::location)
                .orElse(null);
        requireOutletLocation(outletLocation);
        VisitPunch punch = buildPunch(checkOutTime, request.getLocation(), outletLocation);
        requireWithinOutletRadius(punch, "check out");

        current.setCheckOut(punch);
        current.setDurationMinutes(Duration.between(current.getCheckIn().getTime(), checkOutTime).toMinutes());
        current.setOutcome(outcome);
        current.setOutcomeRemarks(remarks);
        current.setStatus(VisitStatus.COMPLETED);

        // Last outlet done: the visit is complete
        boolean anyLeft = visit.getOutletVisits().stream().anyMatch(o -> o.getStatus() == VisitStatus.PLANNED);
        if (!anyLeft) {
            finishVisit(visit, punch, null, currentUser.getId(), "All outlets visited", now);
        }
        visit.setUpdatedBy(currentUser.getId());
        return toResponse(visitRepository.save(visit));
    }

    // =====================================================================
    // End the whole visit early
    // =====================================================================

    @Override
    public VisitResponse endVisit(String visitId, EndVisitRequest request, User currentUser) {
        Visit visit = loadOwnedVisit(visitId, currentUser);
        if (visit.getStatus() != VisitStatus.IN_PROGRESS) {
            throw badRequest("Only an IN_PROGRESS visit can be ended. Current status: " + visit.getStatus());
        }
        VisitOutlet current = currentOutlet(visit);
        if (current != null) {
            throw badRequest("Check out from " + current.getOutletCode() + " first");
        }

        Instant now = Instant.now();
        Instant endTime = resolveDeviceTime(request.getCheckOutTime(), now);
        if (visit.getCheckIn() != null && endTime.isBefore(visit.getCheckIn().getTime())) {
            throw badRequest("checkOutTime is before the start of the visit");
        }
        VisitPunch punch = buildPunch(endTime, request.getLocation(), null);

        finishVisit(visit, punch, blankToNull(request.getRemarks()), currentUser.getId(), "Visit ended by user", now);
        visit.setUpdatedBy(currentUser.getId());
        return toResponse(visitRepository.save(visit));
    }

    /** Outlets not visited become MISSED, the visit gets its end time, summary outcome and COMPLETED. */
    private void finishVisit(Visit visit, VisitPunch endPunch, String remarks, String by, String historyRemarks, Instant now) {
        for (VisitOutlet o : visit.getOutletVisits()) {
            if (o.getStatus() == VisitStatus.PLANNED) {
                o.setStatus(VisitStatus.MISSED);
            }
        }
        visit.setCheckOut(endPunch);
        if (visit.getCheckIn() != null && endPunch != null) {
            visit.setDurationMinutes(Duration.between(visit.getCheckIn().getTime(), endPunch.getTime()).toMinutes());
        }
        visit.setOutcome(summaryOutcome(visit));
        if (remarks != null) {
            visit.setOutcomeRemarks(remarks);
        }
        changeStatus(visit, VisitStatus.COMPLETED, by, historyRemarks, now);
    }

    private VisitOutcome summaryOutcome(Visit visit) {
        Set<VisitOutcome> found = EnumSet.noneOf(VisitOutcome.class);
        for (VisitOutlet o : visit.getOutletVisits()) {
            if (o.getOutcome() != null) {
                found.add(o.getOutcome());
            }
        }
        for (VisitOutcome p : OUTCOME_PRIORITY) {
            if (found.contains(p)) {
                return p;
            }
        }
        return found.isEmpty() ? null : found.iterator().next();
    }

    // =====================================================================
    // GPS track
    // =====================================================================

    /** Points beyond the limit of 500 per visit are ignored, not treated as an error. */
    @Override
    public VisitResponse addTrackPoints(String visitId, AddTrackPointsRequest request, User currentUser) {
        Visit visit = loadOwnedVisit(visitId, currentUser);
        if (visit.getStatus() != VisitStatus.IN_PROGRESS) {
            throw badRequest("GPS points can be added only while the visit is IN_PROGRESS");
        }

        int room = MAX_TRACK_POINTS - visit.getTrackPoints().size();
        if (room <= 0) {
            return toResponse(visit);
        }

        Instant now = Instant.now();
        List<TrackPoint> incoming = request.getPoints().stream()
                .map(p -> TrackPoint.builder()
                        .location(new GeoJsonPoint(p.getLongitude(), p.getLatitude()))
                        .accuracyMeters(p.getAccuracyMeters())
                        .recordedAt(resolveDeviceTime(p.getRecordedAt(), now))
                        .build())
                .sorted(Comparator.comparing(TrackPoint::getRecordedAt))
                .limit(room)
                .toList();

        // Keep the whole stored list in time order, even when an offline batch arrives late
        visit.getTrackPoints().addAll(incoming);
        visit.getTrackPoints().sort(Comparator.comparing(TrackPoint::getRecordedAt));
        visit.setUpdatedBy(currentUser.getId());
        return toResponse(visitRepository.save(visit));
    }

    // =====================================================================
    // Cancel
    // =====================================================================

    @Override
    public VisitResponse cancelVisit(String visitId, CancelVisitRequest request, User currentUser) {
        Visit visit = loadVisibleVisit(visitId, currentUser);
        if (!isOwner(visit, currentUser) && !isApprover(visit, currentUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to cancel this visit");
        }
        if (visit.getStatus() == VisitStatus.IN_PROGRESS && activeOrderCount(visit.getId()) > 0) {
            throw badRequest("This visit has orders, so it cannot be cancelled");
        }

        String reason = request.getReason().trim();
        changeStatus(visit, VisitStatus.CANCELLED, currentUser.getId(), reason, Instant.now());
        for (VisitOutlet o : visit.getOutletVisits()) {
            if (o.getStatus() == VisitStatus.PLANNED || o.getStatus() == VisitStatus.IN_PROGRESS) {
                o.setStatus(VisitStatus.CANCELLED);
            }
        }
        visit.setCancellationReason(reason);
        visit.setUpdatedBy(currentUser.getId());

        return toResponse(visitRepository.save(visit));
    }

    // =====================================================================
    // Read
    // =====================================================================

    @Override
    public VisitResponse getVisitById(String visitId, User currentUser) {
        return toResponse(loadVisibleVisit(visitId, currentUser));
    }

    @Override
    public List<VisitSummaryResponse> searchVisits(VisitSearchRequest request, User currentUser) {
        LocalDate from = request.getFromDate();
        LocalDate to = request.getToDate();

        // No dates given: default to the last 7 days
        if (from == null && to == null) {
            to = LocalDate.now(IST);
            from = to.minusDays(DEFAULT_SEARCH_DAYS);
        }
        if (from != null && to != null && to.isBefore(from)) {
            throw badRequest("toDate cannot be before fromDate");
        }

        VisitFilter filter = new VisitFilter(
                viewerFilterId(currentUser),
                blankToNull(request.getUserId()),
                blankToNull(request.getOutletCode()),
                blankToNull(request.getBeatId()),
                request.getStatus(),
                from != null ? from.toString() : null,
                to != null ? to.toString() : null);

        return visitRepository.search(filter).stream().map(this::toSummary).toList();
    }

    @Override
    public VisitorAttendancePageResponse getVisitorAttendance(
            VisitorAttendanceSearchRequest request,
            User currentUser) {
        if (currentUser == null || currentUser.getRole() != UserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only administrators can view visitor attendance");
        }
        if (request.getPage() < 0 || request.getSize() < 1 || request.getSize() > 100) {
            throw badRequest("page must be non-negative and size must be between 1 and 100");
        }

        LocalDate today = LocalDate.now(IST);
        LocalDate from = request.getFromDate();
        LocalDate to = request.getToDate();
        if (from == null && to == null) {
            to = today;
            from = to.minusDays(DEFAULT_SEARCH_DAYS - 1L);
        } else if (from == null) {
            from = to.minusDays(DEFAULT_SEARCH_DAYS - 1L);
        } else if (to == null) {
            to = today;
        }
        if (to.isBefore(from)) {
            throw badRequest("toDate cannot be before fromDate");
        }

        VisitFilter filter = new VisitFilter(
                null,
                null,
                null,
                null,
                request.getStatus(),
                from.toString(),
                to.toString());
        long offset = (long) request.getPage() * request.getSize();
        List<Visit> visits = visitRepository.searchAttendance(filter, offset, request.getSize());
        long total = visitRepository.countAttendance(filter);

        return VisitorAttendancePageResponse.builder()
                .items(toVisitorAttendanceItems(visits))
                .page(request.getPage())
                .size(request.getSize())
                .totalElements(total)
                .totalPages((int) ((total + request.getSize() - 1) / request.getSize()))
                .build();
    }

    private List<VisitorAttendanceItemResponse> toVisitorAttendanceItems(List<Visit> visits) {
        Set<String> visitorIds = visits.stream()
                .map(Visit::getUserId)
                .map(this::blankToNull)
                .filter(id -> id != null)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        Map<String, User> usersById = new LinkedHashMap<>();
        userRepository.findAllById(visitorIds).forEach(user -> usersById.put(user.getId(), user));

        Set<String> managerIds = new LinkedHashSet<>();
        for (Visit visit : visits) {
            String managerId = managerIdFor(visit, usersById.get(visit.getUserId()));
            if (managerId != null) {
                managerIds.add(managerId);
            }
        }
        userRepository.findAllById(managerIds).forEach(user -> usersById.put(user.getId(), user));

        List<VisitorAttendanceItemResponse> items = new ArrayList<>();
        for (Visit visit : visits) {
            User visitor = usersById.get(visit.getUserId());
            String managerId = managerIdFor(visit, visitor);
            User manager = managerId == null ? null : usersById.get(managerId);

            List<VisitOutlet> outletVisits = visit.getOutletVisits();
            if (outletVisits == null || outletVisits.isEmpty()) {
                outletVisits = visit.getOutletCodes() == null
                        ? List.of()
                        : visit.getOutletCodes().stream()
                                .map(code -> VisitOutlet.builder()
                                        .outletCode(code)
                                        .status(visit.getStatus())
                                        .build())
                                .toList();
            }
            for (VisitOutlet outlet : outletVisits) {
                items.add(toVisitorAttendanceItem(visit, outlet, visitor, manager));
            }
        }
        return items;
    }

    private String managerIdFor(Visit visit, User visitor) {
        String managerId = visitor == null ? null : blankToNull(visitor.getManagerId());
        if (managerId == null && visit.getUserPath() != null && !visit.getUserPath().isEmpty()) {
            managerId = blankToNull(visit.getUserPath().get(visit.getUserPath().size() - 1));
        }
        return managerId;
    }

    private VisitorAttendanceItemResponse toVisitorAttendanceItem(
            Visit visit,
            VisitOutlet outlet,
            User visitor,
            User manager) {
        return VisitorAttendanceItemResponse.builder()
                .visitId(visit.getVisitId())
                .visitDay(parseDay(visit.getVisitDay()))
                .visitorName(displayName(visitor, visit.getUserName()))
                .managerName(displayName(manager, null))
                .outletCode(outlet.getOutletCode())
                .outletName(outlet.getOutletName())
                .photo(outlet.getPictureWithOutlet())
                .checkInTime(outlet.getCheckIn() != null
                        ? outlet.getCheckIn().getTime()
                        : visit.getCheckIn() != null ? visit.getCheckIn().getTime() : null)
                .checkOutTime(outlet.getCheckOut() != null
                        ? outlet.getCheckOut().getTime()
                        : visit.getCheckOut() != null ? visit.getCheckOut().getTime() : null)
                .durationMinutes(outlet.getDurationMinutes() != null
                        ? outlet.getDurationMinutes()
                        : visit.getDurationMinutes())
                .outcome(outlet.getOutcome() != null ? outlet.getOutcome() : visit.getOutcome())
                .remarks(outlet.getOutcomeRemarks() != null
                        ? outlet.getOutcomeRemarks()
                        : visit.getOutcomeRemarks())
                .status(outlet.getStatus() != null ? outlet.getStatus() : visit.getStatus())
                .build();
    }

    private String displayName(User user, String fallback) {
        if (user != null) {
            String fullName = blankToNull(user.getFullName());
            if (fullName != null) {
                return fullName;
            }
            String userName = blankToNull(user.getUsername());
            if (userName != null) {
                return userName;
            }
        }
        return blankToNull(fallback);
    }

    @Override
    public List<VisitSummaryResponse> getMyTodayVisits(User currentUser) {
        String today = LocalDate.now(IST).toString();
        return visitRepository.findByUserIdAndVisitDayOrderByCreatedAtAsc(currentUser.getId(), today).stream()
                .map(this::toSummary)
                .toList();
    }

    @Override
    public List<VisitSummaryResponse> getActiveVisits(User currentUser) {
        VisitFilter filter = new VisitFilter(
                viewerFilterId(currentUser), null, null, null, VisitStatus.IN_PROGRESS, null, null);
        return visitRepository.search(filter).stream().map(this::toSummary).toList();
    }

    // =====================================================================
    // Nightly job
    // =====================================================================

    @Override
    public int markOverduePlannedVisitsAsMissed() {
        String today = LocalDate.now(IST).toString();
        Instant now = Instant.now();
        List<Visit> changed = new ArrayList<>();

        // Planned but never started
        for (Visit visit : visitRepository.findByStatusAndVisitDayLessThan(VisitStatus.PLANNED, today)) {
            changeStatus(visit, VisitStatus.MISSED, "system", "Planned day passed without a check-in", now);
            for (VisitOutlet o : visit.getOutletVisits()) {
                if (o.getStatus() == VisitStatus.PLANNED) {
                    o.setStatus(VisitStatus.MISSED);
                }
            }
            changed.add(visit);
        }

        // Started but never ended: close it so the user can start a new visit
        for (Visit visit : visitRepository.findByStatusAndVisitDayLessThan(VisitStatus.IN_PROGRESS, today)) {
            for (VisitOutlet o : visit.getOutletVisits()) {
                if (o.getStatus() == VisitStatus.IN_PROGRESS) {
                    o.setStatus(VisitStatus.COMPLETED);
                    o.setOutcome(VisitOutcome.OTHER);
                    o.setOutcomeRemarks("Closed automatically: no check-out");
                }
            }
            finishVisit(visit, null, "Closed automatically by the nightly job", "system",
                    "Day passed without ending the visit", now);
            changed.add(visit);
        }

        if (!changed.isEmpty()) {
            visitRepository.saveAll(changed);
        }
        return changed.size();
    }

    // =====================================================================
    // Outlets
    // =====================================================================

    /** What the visit module needs to know about an outlet. */
    private record OutletInfo(
            String id,
            String outletCode,
            String beatId,
            GeoJsonPoint location,
            boolean active,
            String name,
            String phoneNumber,
            OutletAddress address) {
    }

    /** The app may send the outlet's Mongo id or its outlet code; both work. */
    private Optional<OutletInfo> findOutlet(String outletCode) {
        return outletRepository.findById(outletCode)
                .or(() -> outletRepository.findByOutletCode(outletCode))
                .map(o -> new OutletInfo(
                        o.getId(),
                        o.getOutletCode(),
                        o.getBeatId(),
                        o.getLocation(),
                        o.getStatus() == OutletStatus.ACTIVE,
                        o.getName(),
                        o.getPhoneNumber(),
                        o.getAddress()));
    }

    private boolean isOutletInBeat(OutletInfo outlet, String outletReference, Beat beat) {
        return (outlet.beatId() != null && beat.getBeatId().equals(outlet.beatId()))
                || (beat.getOutletCodes() != null
                && (beat.getOutletCodes().contains(outletReference)
                || beat.getOutletCodes().contains(outlet.id())
                || beat.getOutletCodes().contains(outlet.outletCode())));
    }

    private VisitOutlet addOutlet(
            Visit visit,
            String outletCode,
            boolean planned,
            String beatId,
            OutletInfo outlet) {
        VisitOutlet entry = VisitOutlet.builder()
                .outletCode(outletCode)
                .beatId(beatId)
                .outletName(outlet.name())
                .phoneNumber(outlet.phoneNumber())
                .address(outlet.address())
                .planned(planned)
                .status(VisitStatus.PLANNED)
                .build();
        visit.getOutletCodes().add(outletCode);
        visit.getOutletVisits().add(entry);
        return entry;
    }

    private VisitOutlet outletOf(Visit visit, String outletCode) {
        return visit.getOutletVisits().stream()
                .filter(o -> outletCode.equals(o.getOutletCode()))
                .findFirst().orElse(null);
    }

    /** The outlet the user is checked in at right now, if any. */
    private VisitOutlet currentOutlet(Visit visit) {
        return visit.getOutletVisits().stream()
                .filter(o -> o.getStatus() == VisitStatus.IN_PROGRESS)
                .findFirst().orElse(null);
    }

    // =====================================================================
    // Building and changing visits
    // =====================================================================

    private Visit newVisit(User user, String visitDay, boolean planned, String beatId, Instant now, String remarks) {
        // Copy of the user's ancestors; managers' visit lists depend on it
        List<String> userPath = new ArrayList<>();
        if (user.getPath() != null) {
            userPath.addAll(user.getPath());
        }

        Visit visit = Visit.builder()
                .visitId(String.format("VI%09d", sequenceGenerator.next("Visit")))   // VI000000001
                .userId(user.getId())
                .userName(user.getUsername())
                .userPath(userPath)
                .beatId(beatId)
                .visitDay(visitDay)
                .planned(planned)
                .status(VisitStatus.PLANNED)
                .createdBy(user.getId())
                .updatedBy(user.getId())
                .build();

        visit.getStatusHistory().add(VisitStatusHistory.builder()
                .fromStatus(null)
                .toStatus(VisitStatus.PLANNED)
                .updatedBy(user.getId())
                .remarks(remarks)
                .updatedAt(now)
                .build());
        return visit;
    }

    private void changeStatus(Visit visit, VisitStatus to, String by, String remarks, Instant now) {
        VisitStatus from = visit.getStatus();
        if (!TRANSITIONS.getOrDefault(from, Set.of()).contains(to)) {
            throw badRequest("Cannot change a visit from " + from + " to " + to);
        }
        visit.setStatus(to);
        visit.getStatusHistory().add(VisitStatusHistory.builder()
                .fromStatus(from)
                .toStatus(to)
                .updatedBy(by)
                .remarks(remarks)
                .updatedAt(now)
                .build());
    }

    private VisitPunch buildPunch(Instant time, GeoPointDto where, GeoJsonPoint outletLocation) {
        Double distance = null;
        Boolean withinRange = null;
        if (outletLocation != null) {
            double meters = GeoUtils.distanceMeters(
                    where.getLatitude(), where.getLongitude(), outletLocation.getY(), outletLocation.getX());
            distance = Math.round(meters * 10.0) / 10.0;
            withinRange = meters <= checkInRadiusMeters;
        }
        return VisitPunch.builder()
                .time(time)
                .location(new GeoJsonPoint(where.getLongitude(), where.getLatitude()))
                .accuracyMeters(where.getAccuracyMeters())
                .distanceFromOutletMeters(distance)
                .withinRange(withinRange)
                .build();
    }

    private void requireOutletLocation(GeoJsonPoint outletLocation) {
        if (outletLocation == null) {
            throw badRequest("This outlet has no saved latitude and longitude. Ask an administrator to update its location before checking in or out.");
        }
    }

    private void requireWithinOutletRadius(VisitPunch punch, String action) {
        if (enforceGeofence && Boolean.FALSE.equals(punch.getWithinRange())) {
            throw badRequest(String.format("You are %.0f m from the outlet. Move within %.0f m to %s.",
                    punch.getDistanceFromOutletMeters(), checkInRadiusMeters, action));
        }
    }

    /** Accepts the phone's time (offline work) but rejects times in the future or too far in the past. */
    private Instant resolveDeviceTime(Instant deviceTime, Instant now) {
        if (deviceTime == null) {
            return now;
        }
        if (deviceTime.isAfter(now.plus(MAX_CLOCK_SKEW))) {
            throw badRequest("A time sent is in the future. Check the device clock.");
        }
        if (deviceTime.isBefore(now.minus(MAX_OFFLINE_AGE))) {
            throw badRequest("A time sent is older than " + MAX_OFFLINE_AGE.toDays() + " days");
        }
        return deviceTime;
    }

    /**
     * Checks the photo with ImageKit: it must exist, be in the VisitImages folder,
     * be recent (no old photo re-used) and not be attached to another check-in already.
     */
    private MediaRef loadCheckInPhoto(String fileId, User user, Instant now) {
        MediaRef photo = imageKitService.verifyUploadedFile(fileId, MediaFolder.VISIT_IMAGES, user.getId());
        if (photo.getUploadedAt() != null && photo.getUploadedAt().isBefore(now.minus(MAX_OFFLINE_AGE))) {
            throw badRequest("This photo is older than " + MAX_OFFLINE_AGE.toDays() + " days. Take a new photo.");
        }
        if (visitRepository.existsByOutletVisitsPictureWithOutletFileId(fileId)) {
            throw badRequest("This photo is already used for another check-in. Take a new photo.");
        }
        return photo;
    }

    /** Orders of this visit that are still alive (not CANCELLED or RETURNED). */
    private long activeOrderCount(String visitMongoId) {
        return orderRepository.countByVisitIdAndStatusNotIn(visitMongoId, DEAD_ORDER_STATUSES);
    }

    // =====================================================================
    // Permissions and loading
    // =====================================================================

    private void requireVisitRole(User user) {
        if (user == null || !VISIT_ROLES.contains(user.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Your role cannot make visits");
        }
    }

    private boolean isOwner(Visit visit, User user) {
        return user.getId().equals(visit.getUserId());
    }

    /** A manager above the visit's owner: in the owner's hierarchy path, or admin / national head. */
    private boolean isApprover(Visit visit, User user) {
        if (isOwner(visit, user)) {
            return false;
        }
        if (SEE_ALL_ROLES.contains(user.getRole())) {
            return true;
        }
        return visit.getUserPath() != null && visit.getUserPath().contains(user.getId());
    }

    private boolean canView(Visit visit, User user) {
        return isOwner(visit, user) || isApprover(visit, user);
    }

    /** By tracking number (VI000000001) or by Mongo id. */
    private Optional<Visit> findVisit(String key) {
        return visitRepository.findByVisitId(key).or(() -> visitRepository.findById(key));
    }

    /** Hidden visits look like missing visits (404), so their existence is not revealed. */
    private Visit loadVisibleVisit(String visitId, User user) {
        return findVisit(visitId)
                .filter(visit -> canView(visit, user))
                .orElseThrow(() -> new ResourceNotFoundException("Visit not found: " + visitId));
    }

    /** Only the person who owns the visit may change it. */
    private Visit loadOwnedVisit(String visitId, User user) {
        return findVisit(visitId)
                .filter(visit -> isOwner(visit, user))
                .orElseThrow(() -> new ResourceNotFoundException("Visit not found: " + visitId));
    }

    /** null = may see everything; otherwise the id used to match own and subordinate visits. */
    private String viewerFilterId(User user) {
        return SEE_ALL_ROLES.contains(user.getRole()) ? null : user.getId();
    }

    // =====================================================================
    // Mapping (entity to DTO)
    // =====================================================================

    private VisitResponse toResponse(Visit visit) {
        return VisitResponse.builder()
                .visitId(visit.getVisitId())
                .clientVisitId(visit.getClientVisitId())
                .userId(visit.getUserId())
                .userName(visit.getUserName())
                .outletCodes(visit.getOutletCodes())
                .outletVisits(visit.getOutletVisits().stream()
                        .map(outlet -> toOutletResponse(outlet, visit.getBeatId())).toList())
                .beatId(visit.getBeatId())
                .visitDay(parseDay(visit.getVisitDay()))
                .planned(visit.isPlanned())
                .status(visit.getStatus())
                .checkIn(toPunch(visit.getCheckIn()))
                .checkOut(toPunch(visit.getCheckOut()))
                .durationMinutes(visit.getDurationMinutes())
                .outcome(visit.getOutcome())
                .outcomeRemarks(visit.getOutcomeRemarks())
                .cancellationReason(visit.getCancellationReason())
                .orderCount(visit.getId() == null ? 0 : (int) activeOrderCount(visit.getId()))
                .trackPoints(visit.getTrackPoints() == null ? List.of()
                        : visit.getTrackPoints().stream().map(this::toTrackPointDto).toList())
                .statusHistory(visit.getStatusHistory())
                .createdAt(visit.getCreatedAt())
                .updatedAt(visit.getUpdatedAt())
                .build();
    }

    private VisitOutletResponse toOutletResponse(VisitOutlet o, String visitBeatId) {
        OutletInfo details = outletDetailsFor(o);
        return VisitOutletResponse.builder()
                .outletCode(o.getOutletCode())
                .beatId(o.getBeatId() != null ? o.getBeatId() : visitBeatId)
                .outletName(o.getOutletName() != null ? o.getOutletName() : details != null ? details.name() : null)
                .phoneNumber(o.getPhoneNumber() != null ? o.getPhoneNumber() : details != null ? details.phoneNumber() : null)
                .address(o.getAddress() != null ? o.getAddress() : details != null ? details.address() : null)
                .planned(o.isPlanned())
                .status(o.getStatus())
                .checkIn(toPunch(o.getCheckIn()))
                .checkOut(toPunch(o.getCheckOut()))
                .durationMinutes(o.getDurationMinutes())
                .outcome(o.getOutcome())
                .outcomeRemarks(o.getOutcomeRemarks())
                .pictureWithOutlet(o.getPictureWithOutlet())
                .build();
    }

    private VisitSummaryResponse toSummary(Visit visit) {
        return VisitSummaryResponse.builder()
                .id(visit.getId())
                .visitId(visit.getVisitId())
                .userId(visit.getUserId())
                .userName(visit.getUserName())
                .outletCodes(visit.getOutletCodes())
                .outletVisits(visit.getOutletVisits().stream()
                        .map(outlet -> toOutletSummary(outlet, visit.getBeatId()))
                        .toList())
                .beatId(visit.getBeatId())
                .visitDay(parseDay(visit.getVisitDay()))
                .planned(visit.isPlanned())
                .status(visit.getStatus())
                .checkInTime(visit.getCheckIn() != null ? visit.getCheckIn().getTime() : null)
                .checkOutTime(visit.getCheckOut() != null ? visit.getCheckOut().getTime() : null)
                .durationMinutes(visit.getDurationMinutes())
                .outcome(visit.getOutcome())
                .checkInWithinRange(visit.getCheckIn() != null ? visit.getCheckIn().getWithinRange() : null)
                .build();
    }

    private VisitOutletSummaryResponse toOutletSummary(VisitOutlet outlet, String visitBeatId) {
        OutletInfo details = outletDetailsFor(outlet);
        return VisitOutletSummaryResponse.builder()
                .outletCode(outlet.getOutletCode())
                .beatId(outlet.getBeatId() != null ? outlet.getBeatId() : visitBeatId)
                .outletName(outlet.getOutletName() != null ? outlet.getOutletName() : details != null ? details.name() : null)
                .phoneNumber(outlet.getPhoneNumber() != null
                        ? outlet.getPhoneNumber()
                        : details != null ? details.phoneNumber() : null)
                .address(outlet.getAddress() != null ? outlet.getAddress() : details != null ? details.address() : null)
                .planned(outlet.isPlanned())
                .status(outlet.getStatus())
                .build();
    }

    private OutletInfo outletDetailsFor(VisitOutlet outlet) {
        if (outlet.getOutletName() != null && outlet.getPhoneNumber() != null && outlet.getAddress() != null) {
            return null;
        }
        return findOutlet(outlet.getOutletCode()).orElse(null);
    }

    private PunchResponse toPunch(VisitPunch punch) {
        if (punch == null) {
            return null;
        }
        return PunchResponse.builder()
                .time(punch.getTime())
                .location(toGeo(punch.getLocation(), punch.getAccuracyMeters()))
                .distanceFromOutletMeters(punch.getDistanceFromOutletMeters())
                .withinRange(punch.getWithinRange())
                .build();
    }

    private TrackPointDto toTrackPointDto(TrackPoint point) {
        return TrackPointDto.builder()
                .latitude(point.getLocation().getY())
                .longitude(point.getLocation().getX())
                .accuracyMeters(point.getAccuracyMeters())
                .recordedAt(point.getRecordedAt())
                .build();
    }

    // GeoJSON stores longitude first (x), then latitude (y)
    private GeoPointDto toGeo(GeoJsonPoint point, Double accuracyMeters) {
        if (point == null) {
            return null;
        }
        return GeoPointDto.builder()
                .latitude(point.getY())
                .longitude(point.getX())
                .accuracyMeters(accuracyMeters)
                .build();
    }

    private LocalDate parseDay(String day) {
        return day == null ? null : LocalDate.parse(day);
    }

    // =====================================================================
    // Small helpers
    // =====================================================================

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
