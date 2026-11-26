package com.bnagritech.tradesphere.visit.service.impl;

import com.bnagritech.tradesphere.auth.model.User;
import com.bnagritech.tradesphere.common.enums.*;
import com.bnagritech.tradesphere.common.exception.ResourceNotFoundException;
import com.bnagritech.tradesphere.common.util.GeoUtils;
import com.bnagritech.tradesphere.common.util.SequenceGenerator;
import com.bnagritech.tradesphere.media.dto.MediaFolder;
import com.bnagritech.tradesphere.media.dto.MediaRef;
import com.bnagritech.tradesphere.media.service.ImageKitService;
import com.bnagritech.tradesphere.order.repository.OrderRepository;
import com.bnagritech.tradesphere.outlet.repository.OutletRepository;
import com.bnagritech.tradesphere.visit.dto.AddTrackPointsRequest;
import com.bnagritech.tradesphere.visit.dto.CancelVisitRequest;
import com.bnagritech.tradesphere.visit.dto.CheckInRequest;
import com.bnagritech.tradesphere.visit.dto.CheckOutRequest;
import com.bnagritech.tradesphere.visit.dto.GeoPointDto;
import com.bnagritech.tradesphere.visit.dto.PlanVisitsRequest;
import com.bnagritech.tradesphere.visit.dto.PlanVisitsResponse;
import com.bnagritech.tradesphere.visit.dto.PunchResponse;
import com.bnagritech.tradesphere.visit.dto.TrackPointDto;
import com.bnagritech.tradesphere.visit.dto.VisitResponse;
import com.bnagritech.tradesphere.visit.dto.VisitSearchRequest;
import com.bnagritech.tradesphere.visit.dto.VisitSummaryResponse;
import com.bnagritech.tradesphere.visit.model.TrackPoint;
import com.bnagritech.tradesphere.visit.model.Visit;
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
import java.util.HashMap;
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
    private static final int MAX_OUTLETS_PER_PLAN = 20;   // same limit as PlanVisitsRequest

    // A visit in one of these statuses already covers the outlet for that day,
    // so the same outlet cannot be planned again on the same date.
    // CANCELLED and MISSED do not block: the rep may plan that outlet again.
    private static final Set<VisitStatus> BLOCKS_REPLAN = EnumSet.of(
            VisitStatus.PLANNED, VisitStatus.IN_PROGRESS, VisitStatus.COMPLETED);

    // Field roles that make visits
    private static final Set<UserRole> VISIT_ROLES = EnumSet.of(
            UserRole.SALES_REPRESENTATIVE, UserRole.SALES_OFFICER, UserRole.SUPERVISOR, UserRole.PROMOTER);

    // Roles that may see every visit
    private static final Set<UserRole> SEE_ALL_ROLES = EnumSet.of(UserRole.ADMIN, UserRole.NATIONAL_HEAD);

    // Outcomes that need an explanation
    private static final Set<VisitOutcome> REMARKS_REQUIRED = EnumSet.of(
            VisitOutcome.NO_ORDER, VisitOutcome.FOLLOW_UP_REQUIRED, VisitOutcome.OTHER);

    // Order statuses that do not count as a live order for a visit
    private static final Set<OrderStatus> DEAD_ORDER_STATUSES = EnumSet.of(OrderStatus.CANCELLED, OrderStatus.RETURNED);

    // Allowed status changes. Anything not listed is rejected.
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

    // application.yml:  app.visit.checkin-radius-meters / app.visit.enforce-geofence
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

        // Clean the array: drop blank ids and repeats, keep the order the user sent
        Set<String> outletCodes = new LinkedHashSet<>();
        if (request.getOutletCodes() != null) {
            for (String id : request.getOutletCodes()) {
                String clean = blankToNull(id);
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

        // One query: outlets this user already has a live visit for on this day
        // (PLANNED, IN_PROGRESS or COMPLETED) - these must not be planned again
        Set<String> alreadyCovered = new HashSet<>();
        visitRepository.findByUserIdAndVisitDayAndOutletCodeInAndStatusIn(
                        currentUser.getId(), visitDay, outletCodes, BLOCKS_REPLAN)
                .forEach(v -> alreadyCovered.add(v.getOutletCode()));

        // Load the requested outlets (at most 20; missing ones are simply absent from the map)
        Map<String, OutletInfo> outlets = new HashMap<>();
        for (String code : outletCodes) {
            findOutlet(code).ifPresent(o -> outlets.put(code, o));
        }

        List<Visit> saved = new ArrayList<>();
        List<String> skipped = new ArrayList<>();

        for (String outletCode : outletCodes) {
            OutletInfo outlet = outlets.get(outletCode);
            if (alreadyCovered.contains(outletCode) || outlet == null || !outlet.active()) {
                skipped.add(outletCode);
                continue;
            }
            Visit visit = newVisit(currentUser, outlet, request.getBeatId(), visitDay, true,
                    VisitStatus.PLANNED, now, "Visit planned");
            try {
                saved.add(visitRepository.save(visit));
            } catch (DuplicateKeyException e) {
                // A parallel request planned the same outlet for the same day a moment earlier
                skipped.add(outletCode);
            }
        }

        return PlanVisitsResponse.builder()
                .requested(outletCodes.size())
                .created(saved.size())
                .visits(saved.stream().map(this::toSummary).toList())
                .skippedOutletIds(skipped)
                .build();
    }

    // =====================================================================
    // Check-in / track / check-out / cancel
    // =====================================================================

    @Override
    public VisitResponse checkIn(CheckInRequest request, User currentUser) {
        requireVisitRole(currentUser);

        // A retried request carries the same clientVisitId: return the visit already created
        String clientVisitId = blankToNull(request.getClientVisitId());
        if (clientVisitId != null) {
            Optional<Visit> duplicate = visitRepository.findByClientVisitId(clientVisitId);
            if (duplicate.isPresent()) {
                return toResponse(ownedOrConflict(duplicate.get(), currentUser));
            }
        }

        Instant now = Instant.now();
        Instant checkInTime = resolveDeviceTime(request.getCheckInTime(), now);
        LocalDate checkInDay = checkInTime.atZone(IST).toLocalDate();

        Visit visit;
        String visitId = blankToNull(request.getVisitId());

        if (visitId != null) {
            // Starting a planned visit
            visit = visitRepository.findById(visitId)
                    .orElseThrow(() -> new ResourceNotFoundException("Visit not found: " + visitId));
            if (!isOwner(visit, currentUser)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This visit belongs to another user");
            }
            if (visit.getStatus() == VisitStatus.IN_PROGRESS) {
                return toResponse(visit);   // already started: this is a retry
            }
            if (visit.getStatus() != VisitStatus.PLANNED) {
                throw badRequest("Only a PLANNED visit can be started. Current status: " + visit.getStatus());
            }
            if (request.getOutletCode() != null && !request.getOutletCode().equals(visit.getOutletCode())) {
                throw badRequest("outletCode does not match the planned visit");
            }
            if (LocalDate.parse(visit.getVisitDay()).isAfter(checkInDay)) {
                throw badRequest("This visit is planned for " + visit.getVisitDay());
            }
        } else {
            // Unplanned visit: the rep walked into an outlet that was not on the plan
            String outletCode = blankToNull(request.getOutletCode());
            if (outletCode == null) {
                throw badRequest("visitId or outletCode is required");
            }
            // If this outlet is already planned for the day, start that plan instead of
            // creating a second visit (otherwise the plan would later turn MISSED)
            Optional<Visit> plannedToday = visitRepository.findByUserIdAndVisitDayAndOutletCodeInAndStatusIn(
                            currentUser.getId(), checkInDay.toString(), List.of(outletCode), Set.of(VisitStatus.PLANNED))
                    .stream().findFirst();
            if (plannedToday.isPresent()) {
                visit = plannedToday.get();
            } else {
                OutletInfo outlet = findOutlet(outletCode).orElse(null);
                if (outlet == null || !outlet.active()) {
                    throw badRequest("Outlet not found or inactive: " + outletCode);
                }
                visit = newVisit(currentUser, outlet, request.getBeatId(), checkInDay.toString(), false,
                        VisitStatus.IN_PROGRESS, now, "Checked in without a plan");
            }
        }

        // A user can be checked in to only one outlet at a time.
        // This read gives a friendly message; the unique partial index on Visit
        // (userId where status = IN_PROGRESS) is what really stops two at once.
        Optional<Visit> active = visitRepository.findFirstByUserIdAndStatus(currentUser.getId(), VisitStatus.IN_PROGRESS);
        if (active.isPresent()) {
            throw alreadyCheckedIn(active.get());
        }

        VisitPunch punch = buildPunch(checkInTime, request.getLocation(), visit.getOutletLocation());
        if (enforceGeofence && Boolean.FALSE.equals(punch.getWithinRange())) {
            throw badRequest(String.format("You are %.0f m from the outlet. Check in within %.0f m.",
                    punch.getDistanceFromOutletMeters(), checkInRadiusMeters));
        }

        // Check-in photo: the app uploaded it to ImageKit (VisitImages) and sends only the fileId
        String photoFileId = blankToNull(request.getPhotoFileId());
        if (photoFileId == null && requireCheckInPhoto) {
            throw badRequest("A photo is required to check in");
        }
        if (photoFileId != null) {
            punch.setPhoto(loadCheckInPhoto(photoFileId, currentUser, now));
        }

        visit.setCheckIn(punch);
        if (clientVisitId != null) {
            visit.setClientVisitId(clientVisitId);
        }
        if (visit.getBeatId() == null && request.getBeatId() != null) {
            visit.setBeatId(request.getBeatId());
        }
        if (visit.getStatus() == VisitStatus.PLANNED) {
            changeStatus(visit, VisitStatus.IN_PROGRESS, currentUser.getId(), "Checked in", now);
        }
        visit.setUpdatedBy(currentUser.getId());

        try {
            return toResponse(visitRepository.save(visit));
        } catch (DuplicateKeyException e) {
            // Case 1: two requests with the same clientVisitId arrived at the same moment
            if (clientVisitId != null) {
                Optional<Visit> winner = visitRepository.findByClientVisitId(clientVisitId);
                if (winner.isPresent()) {
                    return toResponse(ownedOrConflict(winner.get(), currentUser));
                }
            }
            // Case 2 (FIX): another check-in for this user won the race (one-active-visit index)
            Optional<Visit> other = visitRepository.findFirstByUserIdAndStatus(currentUser.getId(), VisitStatus.IN_PROGRESS);
            if (other.isPresent()) {
                throw alreadyCheckedIn(other.get());
            }
            throw e;
        }
    }

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

        // FIX: keep the whole stored list in time order, even when an offline batch arrives late
        visit.getTrackPoints().addAll(incoming);
        visit.getTrackPoints().sort(Comparator.comparing(TrackPoint::getRecordedAt));
        visit.setUpdatedBy(currentUser.getId());
        return toResponse(visitRepository.save(visit));
    }

    @Override
    public VisitResponse checkOut(String visitId, CheckOutRequest request, User currentUser) {
        Visit visit = loadOwnedVisit(visitId, currentUser);
        if (visit.getStatus() != VisitStatus.IN_PROGRESS) {
            throw badRequest("Only an IN_PROGRESS visit can be checked out. Current status: " + visit.getStatus());
        }
        if (visit.getCheckIn() == null || visit.getCheckIn().getTime() == null) {
            throw badRequest("This visit has no check-in time");
        }

        Instant now = Instant.now();
        Instant checkOutTime = resolveDeviceTime(request.getCheckOutTime(), now);
        if (checkOutTime.isBefore(visit.getCheckIn().getTime())) {
            throw badRequest("checkOutTime is before the check-in time");
        }

        VisitOutcome outcome = request.getOutcome();
        String remarks = blankToNull(request.getRemarks());
        if (REMARKS_REQUIRED.contains(outcome) && remarks == null) {
            throw badRequest("remarks are required for outcome " + outcome);
        }

        // The outcome must agree with the orders actually taken in this visit
        long orders = activeOrderCount(visit.getId());
        if (outcome == VisitOutcome.ORDER_TAKEN && orders == 0) {
            throw badRequest("No order was taken in this visit. Choose another outcome.");
        }
        if (outcome == VisitOutcome.NO_ORDER && orders > 0) {
            throw badRequest("An order exists for this visit, so the outcome cannot be NO_ORDER");
        }

        visit.setCheckOut(buildPunch(checkOutTime, request.getLocation(), visit.getOutletLocation()));
        visit.setDurationMinutes(Duration.between(visit.getCheckIn().getTime(), checkOutTime).toMinutes());
        visit.setOutcome(outcome);
        visit.setOutcomeRemarks(remarks);
        changeStatus(visit, VisitStatus.COMPLETED, currentUser.getId(), "Checked out: " + outcome, now);
        visit.setUpdatedBy(currentUser.getId());

        return toResponse(visitRepository.save(visit));
    }

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
                request.getUserId(),
                request.getOutletCode(),
                request.getBeatId(),
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
    // Scheduled job
    // =====================================================================

    @Override
    public int markOverduePlannedVisitsAsMissed() {
        String today = LocalDate.now(IST).toString();
        List<Visit> overdue = visitRepository.findByStatusAndVisitDayLessThan(VisitStatus.PLANNED, today);
        if (overdue.isEmpty()) {
            return 0;
        }
        Instant now = Instant.now();
        for (Visit visit : overdue) {
            changeStatus(visit, VisitStatus.MISSED, "system", "Planned day passed without a check-in", now);
        }
        visitRepository.saveAll(overdue);
        return overdue.size();
    }

    // =====================================================================
    // Outlet lookup
    // =====================================================================

    /** What the visit module needs to know about an outlet. */
    private record OutletInfo(String outletCode, String name, GeoJsonPoint location, boolean active) {
    }

    /** Finds the outlet by its outletCode. Returns empty for a missing outlet, so each caller decides whether that is an error or a skip. */
    private Optional<OutletInfo> findOutlet(String outletCode) {
        return outletRepository.findByOutletCode(outletCode)
                .map(o -> new OutletInfo(o.getOutletCode(), o.getName(), o.getLocation(),
                        o.getStatus() == OutletStatus.ACTIVE));
    }

    // =====================================================================
    // Building and changing visits
    // =====================================================================

    private Visit newVisit(User user, OutletInfo outlet, String beatId, String visitDay, boolean planned,
                           VisitStatus status, Instant now, String remarks) {
        // Copy of the user's ancestors; managers' visit lists depend on it
        List<String> userPath = new ArrayList<>();
        if (user.getPath() != null) {
            userPath.addAll(user.getPath());
        }

        Visit visit = Visit.builder()
                .visitId(String.format("VI-%06d", sequenceGenerator.next("Visit")))   // readable id, one per visit
                .userId(user.getId())
                .userName(user.getUsername())
                .userPath(userPath)
                .outletCode(outlet.outletCode())
                .outletName(outlet.name())
                .outletLocation(outlet.location())
                .beatId(beatId)
                .visitDay(visitDay)
                .planned(planned)
                .status(status)
                .createdBy(user.getId())
                .updatedBy(user.getId())
                .build();

        visit.getStatusHistory().add(VisitStatusHistory.builder()
                .fromStatus(null)
                .toStatus(status)
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
     * be recent (no old photo re-used) and not be attached to another visit already.
     */
    private MediaRef loadCheckInPhoto(String fileId, User user, Instant now) {
        MediaRef photo = imageKitService.verifyUploadedFile(fileId, MediaFolder.VISIT_IMAGES, user.getId());
        if (photo.getUploadedAt() != null && photo.getUploadedAt().isBefore(now.minus(MAX_OFFLINE_AGE))) {
            throw badRequest("This photo is older than " + MAX_OFFLINE_AGE.toDays() + " days. Take a new photo.");
        }
        if (visitRepository.existsByCheckInPhotoFileId(fileId)) {
            throw badRequest("This photo is already used for another visit. Take a new photo.");
        }
        return photo;
    }

    /**
     * Orders of this visit that are still alive (not CANCELLED or RETURNED).
     * FIX: a count query instead of loading every order document.
     */
    private long activeOrderCount(String visitId) {
        return orderRepository.countByVisitIdAndStatusNotIn(visitId, DEAD_ORDER_STATUSES);
    }

    // =====================================================================
    // Permissions
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

    /** Hidden visits look like missing visits (404), so their existence is not revealed. */
    private Visit loadVisibleVisit(String visitId, User user) {
        return visitRepository.findById(visitId)
                .filter(visit -> canView(visit, user))
                .orElseThrow(() -> new ResourceNotFoundException("Visit not found: " + visitId));
    }

    /** Only the person who owns the visit may change it (track, check out). */
    private Visit loadOwnedVisit(String visitId, User user) {
        return visitRepository.findById(visitId)
                .filter(visit -> isOwner(visit, user))
                .orElseThrow(() -> new ResourceNotFoundException("Visit not found: " + visitId));
    }

    private Visit ownedOrConflict(Visit visit, User user) {
        if (!isOwner(visit, user)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "clientVisitId is already used by another user");
        }
        return visit;
    }

    private ResponseStatusException alreadyCheckedIn(Visit active) {
        return new ResponseStatusException(HttpStatus.CONFLICT,
                "You are already checked in at " + active.getOutletName() + ". Check out first.");
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
                .id(visit.getId())
                .visitId(visit.getVisitId())
                .clientVisitId(visit.getClientVisitId())
                .userId(visit.getUserId())
                .userName(visit.getUserName())
                .outletCode(visit.getOutletCode())
                .outletName(visit.getOutletName())
                .outletLocation(toGeo(visit.getOutletLocation(), null))
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

    private VisitSummaryResponse toSummary(Visit visit) {
        return VisitSummaryResponse.builder()
                .id(visit.getId())
                .visitId(visit.getVisitId())
                .userId(visit.getUserId())
                .userName(visit.getUserName())
                .outletCode(visit.getOutletCode())
                .outletName(visit.getOutletName())
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
                .photo(punch.getPhoto())
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
