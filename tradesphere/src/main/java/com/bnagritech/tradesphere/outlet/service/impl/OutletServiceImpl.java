package com.bnagritech.tradesphere.outlet.service.impl;

import com.bnagritech.tradesphere.auth.model.User;
import com.bnagritech.tradesphere.common.enums.OutletStatus;
import com.bnagritech.tradesphere.common.enums.UserRole;
import com.bnagritech.tradesphere.common.util.SequenceGenerator;
import com.bnagritech.tradesphere.outlet.dto.CreateOutletRequest;
import com.bnagritech.tradesphere.outlet.dto.NearByOutletRequest;
import com.bnagritech.tradesphere.outlet.dto.NearByOutletResponse;
import com.bnagritech.tradesphere.outlet.dto.OutletAddressDto;
import com.bnagritech.tradesphere.outlet.dto.OutletLocationDto;
import com.bnagritech.tradesphere.outlet.dto.OutletResponse;
import com.bnagritech.tradesphere.outlet.dto.OutletSearchRequest;
import com.bnagritech.tradesphere.outlet.dto.OutletSummaryResponse;
import com.bnagritech.tradesphere.outlet.dto.UpdateOutletRequest;
import com.bnagritech.tradesphere.outlet.dto.UpdateOutletStatusRequest;
import com.bnagritech.tradesphere.outlet.model.Outlet;
import com.bnagritech.tradesphere.outlet.model.OutletAddress;
import com.bnagritech.tradesphere.outlet.model.OutletStatusHistory;
import com.bnagritech.tradesphere.outlet.repository.OutletDistance;
import com.bnagritech.tradesphere.outlet.repository.OutletFilter;
import com.bnagritech.tradesphere.outlet.repository.OutletRepository;
import com.bnagritech.tradesphere.outlet.service.OutletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutletServiceImpl implements OutletService {

    private static final int DEFAULT_SEARCH_LIMIT = 100;
    private static final int DEFAULT_NEARBY_RADIUS_METERS = 500;
    private static final int DEFAULT_NEARBY_LIMIT = 20;

    // Roles that may create outlets (promoters may not)
    private static final Set<UserRole> CREATE_ROLES = EnumSet.of(
            UserRole.SALES_REPRESENTATIVE, UserRole.SUPERVISOR, UserRole.SALES_OFFICER,
            UserRole.AREA_MANAGER, UserRole.REGIONAL_MANAGER, UserRole.NATIONAL_HEAD, UserRole.ADMIN);

    // Outlets created by these roles are ACTIVE immediately, with no approval step
    private static final Set<UserRole> AUTO_ACTIVE_ROLES = EnumSet.of(
            UserRole.SALES_OFFICER, UserRole.AREA_MANAGER, UserRole.REGIONAL_MANAGER,
            UserRole.NATIONAL_HEAD, UserRole.ADMIN);

    // Roles that may see every outlet in every status
    private static final Set<UserRole> SEE_ALL_ROLES = EnumSet.of(UserRole.ADMIN, UserRole.NATIONAL_HEAD);

    // Allowed status changes. Anything not listed is rejected.
    private static final Map<OutletStatus, Set<OutletStatus>> TRANSITIONS = Map.of(
            OutletStatus.PENDING_APPROVAL, EnumSet.of(OutletStatus.ACTIVE, OutletStatus.REJECTED),
            OutletStatus.ACTIVE, EnumSet.of(OutletStatus.INACTIVE),
            OutletStatus.INACTIVE, EnumSet.of(OutletStatus.ACTIVE),
            OutletStatus.REJECTED, EnumSet.of(OutletStatus.PENDING_APPROVAL)
    );

    private final OutletRepository outletRepository;
    private final SequenceGenerator sequenceGenerator;

    // application.properties:  app.outlet.require-approval / app.outlet.duplicate-radius-meters
    // false = every new outlet is ACTIVE at once, whoever creates it
    @Value("${app.outlet.require-approval:true}")
    private boolean requireApproval = true;

    // A new outlet with the same name this close (in meters) to an existing one is treated as a duplicate
    @Value("${app.outlet.duplicate-radius-meters:30}")
    private double duplicateRadiusMeters = 30.0;

    // =====================================================================
    // Create / update / status
    // =====================================================================

    @Override
    public OutletResponse createOutlet(CreateOutletRequest request, User currentUser) {
        if (currentUser == null || !CREATE_ROLES.contains(currentUser.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Your role cannot create outlets");
        }

        // A retried request carries the same clientOutletId: return the outlet already created
        String clientOutletId = blankToNull(request.getClientOutletId());
        if (clientOutletId != null) {
            Optional<Outlet> existing = outletRepository.findByClientOutletId(clientOutletId);
            if (existing.isPresent()) {
                return toResponse(ownedOrConflict(existing.get(), currentUser));
            }
        }

        String externalCode = blankToNull(request.getExternalCode());
        if (externalCode != null && outletRepository.existsByExternalCode(externalCode)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "externalCode is already used by another outlet");
        }

        GeoJsonPoint point = toPoint(request.getLocation());
        if (!request.isIgnoreDuplicateWarning()) {
            rejectIfDuplicateNearby(request.getName(), point);
        }

        Instant now = Instant.now();
        OutletStatus status = (!requireApproval || AUTO_ACTIVE_ROLES.contains(currentUser.getRole()))
                ? OutletStatus.ACTIVE
                : OutletStatus.PENDING_APPROVAL;

        // Copy of the creator's ancestors; their managers can then see and approve the outlet
        List<String> userPath = new ArrayList<>();
        if (currentUser.getPath() != null) {
            userPath.addAll(currentUser.getPath());
        }

        Outlet outlet = Outlet.builder()
                .outletCode(String.format("OUT-%06d", sequenceGenerator.next("OUTLET")))
                .externalCode(externalCode)
                .clientOutletId(clientOutletId)
                .name(request.getName().trim())
                .ownerName(request.getOwnerName())
                .phoneNumber(request.getPhoneNumber())
                .alternatePhoneNumber(request.getAlternatePhoneNumber())
                .email(request.getEmail())
                .gstNumber(request.getGstNumber())
                .type(request.getType())
                .grade(request.getGrade())
                .address(toAddress(request.getAddress()))
                .location(point)
                .locationAccuracyMeters(request.getLocation().getAccuracyMeters())
                .territoryId(request.getTerritoryId())
                .beatId(request.getBeatId())
                .distributorId(request.getDistributorId())
                .status(status)
                .createdBy(currentUser.getId())
                .createdByName(currentUser.getUsername())
                .userPath(userPath)
                .updatedBy(currentUser.getId())
                .build();

        outlet.getStatusHistory().add(OutletStatusHistory.builder()
                .fromStatus(null)
                .toStatus(status)
                .updatedBy(currentUser.getId())
                .remarks(status == OutletStatus.ACTIVE ? "Outlet created" : "Outlet created, waiting for approval")
                .updatedAt(now)
                .build());

        try {
            return toResponse(outletRepository.save(outlet));
        } catch (DuplicateKeyException e) {
            // Two requests with the same clientOutletId arrived at the same moment
            if (clientOutletId != null) {
                Optional<Outlet> winner = outletRepository.findByClientOutletId(clientOutletId);
                if (winner.isPresent()) {
                    return toResponse(ownedOrConflict(winner.get(), currentUser));
                }
            }
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "An outlet with the same code or externalCode already exists");
        }
    }

    @Override
    public OutletResponse updateOutlet(String outletId, UpdateOutletRequest request, User currentUser) {
        Outlet outlet = loadVisibleOutlet(outletId, currentUser);
        boolean creator = isCreator(outlet, currentUser);
        boolean approver = isApprover(outlet, currentUser);
        require(creator || approver, "You are not allowed to change this outlet");

        // The creator may change sensitive fields only while the outlet is not yet live
        boolean notYetLive = outlet.getStatus() == OutletStatus.PENDING_APPROVAL
                || outlet.getStatus() == OutletStatus.REJECTED;
        boolean privileged = approver || (creator && notYetLive);
        if (!privileged && touchesPrivilegedFields(request)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only a manager can change location, grade, territory, beat or distributor of a live outlet");
        }

        if (request.getName() != null) {
            if (request.getName().isBlank()) {
                throw badRequest("name cannot be blank");
            }
            outlet.setName(request.getName().trim());
        }

        String externalCode = blankToNull(request.getExternalCode());
        if (externalCode != null && !externalCode.equals(outlet.getExternalCode())) {
            if (outletRepository.existsByExternalCode(externalCode)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "externalCode is already used by another outlet");
            }
            outlet.setExternalCode(externalCode);
        }

        if (request.getOwnerName() != null) {
            outlet.setOwnerName(request.getOwnerName());
        }
        if (request.getPhoneNumber() != null) {
            outlet.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getAlternatePhoneNumber() != null) {
            outlet.setAlternatePhoneNumber(request.getAlternatePhoneNumber());
        }
        if (request.getEmail() != null) {
            outlet.setEmail(request.getEmail());
        }
        if (request.getGstNumber() != null) {
            outlet.setGstNumber(request.getGstNumber());
        }
        if (request.getType() != null) {
            outlet.setType(request.getType());
        }
        if (request.getAddress() != null) {
            outlet.setAddress(toAddress(request.getAddress()));
        }

        // Sensitive fields (checked above)
        if (request.getGrade() != null) {
            outlet.setGrade(request.getGrade());
        }
        if (request.getLocation() != null) {
            outlet.setLocation(toPoint(request.getLocation()));
            outlet.setLocationAccuracyMeters(request.getLocation().getAccuracyMeters());
        }
        if (request.getTerritoryId() != null) {
            outlet.setTerritoryId(request.getTerritoryId());
        }
        if (request.getBeatId() != null) {
            outlet.setBeatId(request.getBeatId());
        }
        if (request.getDistributorId() != null) {
            outlet.setDistributorId(request.getDistributorId());
        }

        outlet.setUpdatedBy(currentUser.getId());
        try {
            return toResponse(outletRepository.save(outlet));
        } catch (DuplicateKeyException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "externalCode is already used by another outlet");
        }
    }

    @Override
    public OutletResponse updateOutletStatus(String outletId, UpdateOutletStatusRequest request, User currentUser) {
        Outlet outlet = loadVisibleOutlet(outletId, currentUser);

        OutletStatus from = outlet.getStatus();
        OutletStatus to = request.getStatus();
        if (!TRANSITIONS.getOrDefault(from, Set.of()).contains(to)) {
            throw badRequest("Cannot change an outlet from " + from + " to " + to);
        }

        boolean creator = isCreator(outlet, currentUser);
        boolean approver = isApprover(outlet, currentUser);

        switch (to) {
            case ACTIVE, REJECTED, INACTIVE ->
                    require(approver, "Only a manager above the outlet's creator can set status " + to);
            case PENDING_APPROVAL ->
                    require(creator, "Only the person who created the outlet can resubmit it");
        }

        String reason = blankToNull(request.getRemarks());
        if ((to == OutletStatus.REJECTED || to == OutletStatus.INACTIVE) && reason == null) {
            throw badRequest("remarks (the reason) are required when an outlet is " + to);
        }

        outlet.setStatus(to);
        outlet.setStatusReason((to == OutletStatus.REJECTED || to == OutletStatus.INACTIVE) ? reason : null);
        outlet.setUpdatedBy(currentUser.getId());
        outlet.getStatusHistory().add(OutletStatusHistory.builder()
                .fromStatus(from)
                .toStatus(to)
                .updatedBy(currentUser.getId())
                .remarks(reason)
                .updatedAt(Instant.now())
                .build());

        return toResponse(outletRepository.save(outlet));
    }

    // =====================================================================
    // Read
    // =====================================================================

    @Override
    public OutletResponse getOutletById(String outletId, User currentUser) {
        return toResponse(loadVisibleOutlet(outletId, currentUser));
    }

    @Override
    public OutletResponse getOutletByCode(String outletCode, User currentUser) {
        return outletRepository.findByOutletCode(outletCode)
                .filter(outlet -> canView(outlet, currentUser))
                .map(this::toResponse)
                .orElseThrow(() -> notFound("Outlet not found"));
    }

    @Override
    public List<OutletSummaryResponse> searchOutlets(OutletSearchRequest request, User currentUser) {
        int limit = request.getLimit() != null ? request.getLimit() : DEFAULT_SEARCH_LIMIT;

        OutletFilter filter = new OutletFilter(
                SEE_ALL_ROLES.contains(currentUser.getRole()) ? null : currentUser.getId(),
                request.getKeyword(),
                request.getStatus(),
                request.getType(),
                request.getGrade(),
                request.getBeatId(),
                request.getTerritoryId(),
                request.getCity(),
                request.getPinCode(),
                limit);

        return outletRepository.search(filter).stream().map(this::toSummary).toList();
    }

    @Override
    public List<NearByOutletResponse> findNearbyOutlets(NearByOutletRequest request, User currentUser) {
        int radius = request.getRadiusMeters() != null ? request.getRadiusMeters() : DEFAULT_NEARBY_RADIUS_METERS;
        int limit = request.getLimit() != null ? request.getLimit() : DEFAULT_NEARBY_LIMIT;
        GeoJsonPoint center = new GeoJsonPoint(request.getLongitude(), request.getLatitude());

        return outletRepository.findNearby(center, radius, limit, List.of(OutletStatus.ACTIVE)).stream()
                .map(this::toNearby)
                .toList();
    }

    @Override
    public List<OutletSummaryResponse> getOutletsByBeat(String beatId, User currentUser) {
        return outletRepository.findByBeatIdAndStatusOrderByNameAsc(beatId, OutletStatus.ACTIVE).stream()
                .map(this::toSummary)
                .toList();
    }

    // =====================================================================
    // Rules and permissions
    // =====================================================================

    /** Stops the same shop being entered twice: same name, very close to an existing outlet. */
    private void rejectIfDuplicateNearby(String name, GeoJsonPoint point) {
        String wanted = normalizeName(name);
        List<OutletStatus> considered = List.of(
                OutletStatus.PENDING_APPROVAL, OutletStatus.ACTIVE, OutletStatus.INACTIVE);

        for (OutletDistance near : outletRepository.findNearby(point, duplicateRadiusMeters, 20, considered)) {
            if (normalizeName(near.outlet().getName()).equals(wanted)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, String.format(
                        "An outlet with the same name already exists %.0f m away (%s). "
                                + "If this is a different shop, send ignoreDuplicateWarning=true.",
                        near.distanceMeters(), near.outlet().getOutletCode()));
            }
        }
    }

    private boolean touchesPrivilegedFields(UpdateOutletRequest r) {
        return r.getGrade() != null || r.getLocation() != null || r.getTerritoryId() != null
                || r.getBeatId() != null || r.getDistributorId() != null;
    }

    private boolean isCreator(Outlet outlet, User user) {
        return user.getId().equals(outlet.getCreatedBy());
    }

    /** A manager above the creator: in the creator's hierarchy path, or admin / national head. */
    private boolean isApprover(Outlet outlet, User user) {
        if (isCreator(outlet, user)) {
            return false;
        }
        if (SEE_ALL_ROLES.contains(user.getRole())) {
            return true;
        }
        return outlet.getUserPath() != null && outlet.getUserPath().contains(user.getId());
    }

    /** ACTIVE outlets are visible to everyone; other statuses only to the creator and managers. */
    private boolean canView(Outlet outlet, User user) {
        return outlet.getStatus() == OutletStatus.ACTIVE || isCreator(outlet, user) || isApprover(outlet, user);
    }

    /** Hidden outlets look like missing outlets (404), so their existence is not revealed. */
    private Outlet loadVisibleOutlet(String outletId, User user) {
        return outletRepository.findById(outletId)
                .filter(outlet -> canView(outlet, user))
                .orElseThrow(() -> notFound("Outlet not found"));
    }

    private Outlet ownedOrConflict(Outlet outlet, User user) {
        if (!isCreator(outlet, user)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "clientOutletId is already used by another user");
        }
        return outlet;
    }

    private void require(boolean condition, String message) {
        if (!condition) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, message);
        }
    }

    // =====================================================================
    // Mapping
    // =====================================================================

    private OutletResponse toResponse(Outlet o) {
        return OutletResponse.builder()
                .id(o.getId())
                .outletCode(o.getOutletCode())
                .externalCode(o.getExternalCode())
                .name(o.getName())
                .ownerName(o.getOwnerName())
                .phoneNumber(o.getPhoneNumber())
                .alternatePhoneNumber(o.getAlternatePhoneNumber())
                .email(o.getEmail())
                .gstNumber(o.getGstNumber())
                .type(o.getType())
                .grade(o.getGrade())
                .address(toAddressDto(o.getAddress()))
                .location(toLocationDto(o.getLocation(), o.getLocationAccuracyMeters()))
                .territoryId(o.getTerritoryId())
                .beatId(o.getBeatId())
                .distributorId(o.getDistributorId())
                .status(o.getStatus())
                .statusReason(o.getStatusReason())
                .statusHistory(o.getStatusHistory())
                .createdBy(o.getCreatedBy())
                .createdByName(o.getCreatedByName())
                .createdAt(o.getCreatedAt())
                .updatedAt(o.getUpdatedAt())
                .build();
    }

    private OutletSummaryResponse toSummary(Outlet o) {
        return OutletSummaryResponse.builder()
                .id(o.getId())
                .outletCode(o.getOutletCode())
                .name(o.getName())
                .ownerName(o.getOwnerName())
                .phoneNumber(o.getPhoneNumber())
                .type(o.getType())
                .grade(o.getGrade())
                .city(o.getAddress() != null ? o.getAddress().getCity() : null)
                .pinCode(o.getAddress() != null ? o.getAddress().getPinCode() : null)
                .beatId(o.getBeatId())
                .status(o.getStatus())
                .location(toLocationDto(o.getLocation(), null))
                .build();
    }

    private NearByOutletResponse toNearby(OutletDistance d) {
        Outlet o = d.outlet();
        return NearByOutletResponse.builder()
                .id(o.getId())
                .outletCode(o.getOutletCode())
                .name(o.getName())
                .phoneNumber(o.getPhoneNumber())
                .type(o.getType())
                .addressLine(o.getAddress() != null ? o.getAddress().getLine1() : null)
                .location(toLocationDto(o.getLocation(), null))
                .distanceMeters(Math.round(d.distanceMeters() * 10.0) / 10.0)
                .build();
    }

    private OutletAddress toAddress(OutletAddressDto dto) {
        return OutletAddress.builder()
                .line1(dto.getLine1())
                .line2(dto.getLine2())
                .area(dto.getArea())
                .city(dto.getCity())
                .district(dto.getDistrict())
                .state(dto.getState())
                .pinCode(dto.getPinCode())
                .landmark(dto.getLandmark())
                .build();
    }

    private OutletAddressDto toAddressDto(OutletAddress a) {
        if (a == null) {
            return null;
        }
        return OutletAddressDto.builder()
                .line1(a.getLine1())
                .line2(a.getLine2())
                .area(a.getArea())
                .city(a.getCity())
                .district(a.getDistrict())
                .state(a.getState())
                .pinCode(a.getPinCode())
                .landmark(a.getLandmark())
                .build();
    }

    // GeoJSON stores longitude first (x), then latitude (y)
    private GeoJsonPoint toPoint(OutletLocationDto dto) {
        return new GeoJsonPoint(dto.getLongitude(), dto.getLatitude());
    }

    private OutletLocationDto toLocationDto(GeoJsonPoint point, Double accuracyMeters) {
        if (point == null) {
            return null;
        }
        return OutletLocationDto.builder()
                .latitude(point.getY())
                .longitude(point.getX())
                .accuracyMeters(accuracyMeters)
                .build();
    }

    // =====================================================================
    // Small helpers
    // =====================================================================

    private String normalizeName(String name) {
        return name == null ? "" : name.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

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
