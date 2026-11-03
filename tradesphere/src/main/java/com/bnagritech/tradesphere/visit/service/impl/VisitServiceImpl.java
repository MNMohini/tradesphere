package com.bnagritech.tradesphere.visit.service.impl;

import com.bnagritech.tradesphere.auth.model.User;
import com.bnagritech.tradesphere.common.enums.OrderStatus;
import com.bnagritech.tradesphere.common.enums.UserRole;
import com.bnagritech.tradesphere.common.enums.VisitOutcome;
import com.bnagritech.tradesphere.common.enums.VisitStatus;
import com.bnagritech.tradesphere.common.util.GeoUtils;
import com.bnagritech.tradesphere.order.repository.OrderRepository;
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

    // Field roles that make visits
    private static final Set<UserRole> VISIT_ROLES = EnumSet.of(
            UserRole.SALES_REPRESENTATIVE, UserRole.SALES_OFFICER, UserRole.SUPERVISOR, UserRole.PROMOTER);

    // Roles that may see every visit
    private static final Set<UserRole> SEE_ALL_ROLES = EnumSet.of(UserRole.ADMIN, UserRole.NATIONAL_HEAD);

    // Outcomes that need an explanation
    private static final Set<VisitOutcome> REMARKS_REQUIRED = EnumSet.of(
            VisitOutcome.NO_ORDER, VisitOutcome.FOLLOW_UP_REQUIRED, VisitOutcome.OTHER);

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

        String visitDay = day.toString();
        Instant now = Instant.now();
        Set<String> outletIds = new LinkedHashSet<>(request.getOutletIds());

        List<Visit> toSave = new ArrayList<>();
        List<String> skipped = new ArrayList<>();

        for (String outletId : outletIds) {
            boolean alreadyPlanned = visitRepository.existsByUserIdAndOutletIdAndVisitDayAndStatus(
                    currentUser.getId(), outletId, visitDay, VisitStatus.PLANNED);
            OutletInfo outlet = alreadyPlanned ? null : loadOutlet(outletId);

            if (alreadyPlanned || outlet == null || !outlet.active()) {
                skipped.add(outletId);
                continue;
            }
            toSave.add(newVisit(currentUser, outlet, request.getBeatId(), visitDay, true,
                    VisitStatus.PLANNED, now, "Visit planned"));
        }

        List<Visit> saved = toSave.isEmpty() ? List.of() : visitRepository.saveAll(toSave);

        return PlanVisitsResponse.builder()
                .requested(outletIds.size())
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
            visit = visitRepository.findById(visitId).orElseThrow(() -> notFound("Visit not found"));
            if (!isOwner(visit, currentUser)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This visit belongs to another user");
            }
            if (visit.getStatus() == VisitStatus.IN_PROGRESS) {
                return toResponse(visit);   // already started: this is a retry
            }
            if (visit.getStatus() != VisitStatus.PLANNED) {
                throw badRequest("Only a PLANNED visit can be started. Current status: " + visit.getStatus());
            }
            if (request.getOutletId() != null && !request.getOutletId().equals(visit.getOutletId())) {
                throw badRequest("outletId does not match the planned visit");
            }
            if (LocalDate.parse(visit.getVisitDay()).isAfter(checkInDay)) {
                throw badRequest("This visit is planned for " + visit.getVisitDay());
            }
        } else {
            // Unplanned visit: the rep walked into an outlet that was not on the plan
            String outletId = blankToNull(request.getOutletId());
            if (outletId == null) {
                throw badRequest("visitId or outletId is required");
            }
            OutletInfo outlet = loadOutlet(outletId);
            if (outlet == null || !outlet.active()) {
                throw badRequest("Outlet not found or inactive: " + outletId);
            }
            visit = newVisit(currentUser, outlet, request.getBeatId(), checkInDay.toString(), false,
                    VisitStatus.IN_PROGRESS, now, "Checked in without a plan");
        }

        // A user can be checked in to only one outlet at a time
        Optional<Visit> active = visitRepository.findFirstByUserIdAndStatus(currentUser.getId(), VisitStatus.IN_PROGRESS);
        if (active.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "You are already checked in at " + active.get().getOutletName() + ". Check out first.");
        }

        VisitPunch punch = buildPunch(checkInTime, request.getLocation(), visit.getOutletLocation());
        if (enforceGeofence && Boolean.FALSE.equals(punch.getWithinRange())) {
            throw badRequest(String.format("You are %.0f m from the outlet. Check in within %.0f m.",
                    punch.getDistanceFromOutletMeters(), checkInRadiusMeters));
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
            // Two requests with the same clientVisitId arrived at the same moment
            if (clientVisitId == null) {
                throw e;
            }
            Visit winner = visitRepository.findByClientVisitId(clientVisitId).orElseThrow(() -> e);
            return toResponse(ownedOrConflict(winner, currentUser));
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

        visit.getTrackPoints().addAll(incoming);
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
        int orders = activeOrderCount(visit.getId());
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
                request.getOutletId(),
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
    // TODO: connect to the Outlet module once it exists.
    // Only this method needs to change.
    // =====================================================================

    /** What the visit module needs to know about an outlet. */
    private record OutletInfo(String outletId, String name, GeoJsonPoint location, boolean active) {
    }

    /** Return the outlet, or null if it does not exist. */
    private OutletInfo loadOutlet(String outletId) {
        throw new UnsupportedOperationException("Outlet module is not connected yet");
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
                .userId(user.getId())
                .userName(user.getUsername())
                .userPath(userPath)
                .outletId(outlet.outletId())
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

    /** Orders of this visit that are still alive (not cancelled or rejected). */
    private int activeOrderCount(String visitId) {
        return (int) orderRepository.findByVisitIdOrderByOrderDateDesc(visitId).stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED && o.getStatus() != OrderStatus.REJECTED)
                .count();
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
                .orElseThrow(() -> notFound("Visit not found"));
    }

    /** Only the person who owns the visit may change it (track, check out). */
    private Visit loadOwnedVisit(String visitId, User user) {
        return visitRepository.findById(visitId)
                .filter(visit -> isOwner(visit, user))
                .orElseThrow(() -> notFound("Visit not found"));
    }

    private Visit ownedOrConflict(Visit visit, User user) {
        if (!isOwner(visit, user)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "clientVisitId is already used by another user");
        }
        return visit;
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
                .clientVisitId(visit.getClientVisitId())
                .userId(visit.getUserId())
                .userName(visit.getUserName())
                .outletId(visit.getOutletId())
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
                .orderCount(visit.getId() == null ? 0 : activeOrderCount(visit.getId()))
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
                .userId(visit.getUserId())
                .userName(visit.getUserName())
                .outletId(visit.getOutletId())
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

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }
}