package com.bnagritech.tradesphere.visit.service.impl;

import com.bnagritech.tradesphere.auth.model.User;
import com.bnagritech.tradesphere.common.enums.*;
import com.bnagritech.tradesphere.common.exception.ResourceNotFoundException;
import com.bnagritech.tradesphere.common.util.GeoUtils;
import com.bnagritech.tradesphere.common.util.SequenceGenerator;
import com.bnagritech.tradesphere.imagekit.dto.MediaFolder;
import com.bnagritech.tradesphere.imagekit.dto.MediaRef;
import com.bnagritech.tradesphere.imagekit.service.ImageKitService;
import com.bnagritech.tradesphere.order.repository.OrderRepository;
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
import com.bnagritech.tradesphere.visit.dto.VisitResponse;
import com.bnagritech.tradesphere.visit.dto.VisitSearchRequest;
import com.bnagritech.tradesphere.visit.dto.VisitSummaryResponse;
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
    private final OrderRepository orderRepository;
    private final OutletRepository outletRepository;
    private final ImageKitService imageKitService;
    private final SequenceGenerator sequenceGenerator;

    // true = a check-in without a photo is rejected
    @Value("${app.visit.require-checkin-photo:false}")
    private boolean requireCheckInPhoto = false;

    @Value("${app.visit.checkin-radius-meters:100}")
    private double checkInRadiusMeters = 100.0;

    // false = a check-in far from the outlet is allowed but flagged (withinRange = false)
    // true  = a check-in far from the outlet is rejected
    @Value("${app.visit.enforce-geofence:false}")
    private boolean enforceGeofence = false;

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

        // The user's visits that day. An outlet already planned / visited that day is skipped,
        // and new outlets go into the open visit of that day (no second plan for the same day).
        List<Visit> sameDay = visitRepository.findByUserIdAndVisitDayAndStatusIn(
                currentUser.getId(), visitDay, EnumSet.of(VisitStatus.PLANNED, VisitStatus.IN_PROGRESS, VisitStatus.COMPLETED));
        Set<String> alreadyTaken = new HashSet<>();
        Visit openVisit = null;
        for (Visit v : sameDay) {
            for (VisitOutlet o : v.getOutletVisits()) {
                if (OUTLET_TAKEN.contains(o.getStatus())) {
                    alreadyTaken.add(o.getOutletCode());
                }
            }
            if (openVisit == null && OPEN.contains(v.getStatus())) {
                openVisit = v;
            }
        }

        List<String> toAdd = new ArrayList<>();
        List<String> skipped = new ArrayList<>();
        for (String code : outletCodes) {
            Optional<OutletInfo> outlet = findOutlet(code);
            if (alreadyTaken.contains(code) || outlet.isEmpty() || !outlet.get().active()) {
                skipped.add(code);
            } else {
                toAdd.add(code);
            }
        }

        if (toAdd.isEmpty()) {
            return PlanVisitsResponse.builder()
                    .visitId(openVisit != null ? openVisit.getVisitId() : null)
                    .requested(outletCodes.size())
                    .created(0)
                    .visits(openVisit != null ? List.of(toSummary(openVisit)) : List.of())
                    .skippedOutletIds(skipped)
                    .build();
        }

        Visit visit = openVisit != null
                ? openVisit
                : newVisit(currentUser, visitDay, true, request.getBeatId(), now, "Visit planned");
        for (String code : toAdd) {
            addOutlet(visit, code, true);
        }
        if (visit.getBeatId() == null && request.getBeatId() != null) {
            visit.setBeatId(request.getBeatId());
        }
        visit.setUpdatedBy(currentUser.getId());

        Visit saved;
        try {
            saved = visitRepository.save(visit);
        } catch (DuplicateKeyException e) {
            // Another plan request for the same day was saved a moment earlier
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A visit for " + visitDay + " was just created. Please send the request again.");
        }

        return PlanVisitsResponse.builder()
                .visitId(saved.getVisitId())
                .requested(outletCodes.size())
                .created(toAdd.size())
                .visits(List.of(toSummary(saved)))
                .skippedOutletIds(skipped)
                .build();
    }

    // =====================================================================
    // Check-in at one outlet
    // =====================================================================

    @Override
    public VisitResponse checkIn(CheckInRequest request, User currentUser) {
        requireVisitRole(currentUser);

        String outletCode = blankToNull(request.getOutletCode());
        if (outletCode == null) {
            throw badRequest("outletCode is required");
        }
        String clientVisitId = blankToNull(request.getClientVisitId());

        Instant now = Instant.now();
        Instant checkInTime = resolveDeviceTime(request.getCheckInTime(), now);
        LocalDate checkInDay = checkInTime.atZone(IST).toLocalDate();

        // 1. Which visit?
        Visit visit = findTargetVisit(request, clientVisitId, checkInDay, currentUser);
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

        OutletInfo outlet = findOutlet(outletCode)
                .orElseThrow(() -> badRequest("Outlet not found: " + outletCode));
        if (!outlet.active()) {
            throw badRequest("Outlet is not active: " + outletCode);
        }
        if (target == null) {
            target = addOutlet(visit, outletCode, false);   // walk-in outlet added to this visit
        }

        // 4. Where is the user compared to the outlet?
        VisitPunch punch = buildPunch(checkInTime, request.getLocation(), outlet.location());
        if (enforceGeofence && Boolean.FALSE.equals(punch.getWithinRange())) {
            throw badRequest(String.format("You are %.0f m from the outlet. Check in within %.0f m.",
                    punch.getDistanceFromOutletMeters(), checkInRadiusMeters));
        }

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

    /** visitId given -> that visit; retried walk-in -> its visit; otherwise today's open visit (or null). */
    private Visit findTargetVisit(CheckInRequest request, String clientVisitId, LocalDate checkInDay, User user) {
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

        GeoJsonPoint outletLocation = findOutlet(current.getOutletCode()).map(OutletInfo::location).orElse(null);
        VisitPunch punch = buildPunch(checkOutTime, request.getLocation(), outletLocation);

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
    private record OutletInfo(String outletCode, GeoJsonPoint location, boolean active) {
    }

    /** The app may send the outlet's Mongo id or its outlet code; both work. */
    private Optional<OutletInfo> findOutlet(String outletCode) {
        return outletRepository.findById(outletCode)
                .or(() -> outletRepository.findByOutletCode(outletCode))
                .map(o -> new OutletInfo(outletCode, o.getLocation(), o.getStatus() == OutletStatus.ACTIVE));
    }

    private VisitOutlet addOutlet(Visit visit, String outletCode, boolean planned) {
        VisitOutlet entry = VisitOutlet.builder()
                .outletCode(outletCode)
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
                .outletVisits(visit.getOutletVisits().stream().map(this::toOutletResponse).toList())
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

    private VisitOutletResponse toOutletResponse(VisitOutlet o) {
        return VisitOutletResponse.builder()
                .outletCode(o.getOutletCode())
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
