package com.bnagritech.tradesphere.visit.service.impl;

import com.bnagritech.tradesphere.auth.model.User;
import com.bnagritech.tradesphere.auth.repository.UserRepository;
import com.bnagritech.tradesphere.beat.model.Beat;
import com.bnagritech.tradesphere.beat.repository.BeatRepository;
import com.bnagritech.tradesphere.common.enums.OutletStatus;
import com.bnagritech.tradesphere.common.enums.UserRole;
import com.bnagritech.tradesphere.common.enums.UserStatus;
import com.bnagritech.tradesphere.common.enums.VisitOutcome;
import com.bnagritech.tradesphere.common.enums.VisitStatus;
import com.bnagritech.tradesphere.common.util.SequenceGenerator;
import com.bnagritech.tradesphere.imagekit.service.ImageKitService;
import com.bnagritech.tradesphere.imagekit.dto.MediaRef;
import com.bnagritech.tradesphere.order.repository.OrderRepository;
import com.bnagritech.tradesphere.visit.dto.CheckInRequest;
import com.bnagritech.tradesphere.visit.dto.CheckOutRequest;
import com.bnagritech.tradesphere.visit.dto.GeoPointDto;
import com.bnagritech.tradesphere.visit.dto.VisitorAttendanceSearchRequest;
import com.bnagritech.tradesphere.outlet.model.Outlet;
import com.bnagritech.tradesphere.outlet.model.OutletAddress;
import com.bnagritech.tradesphere.outlet.repository.OutletRepository;
import com.bnagritech.tradesphere.visit.dto.PlanVisitsRequest;
import com.bnagritech.tradesphere.visit.model.Visit;
import com.bnagritech.tradesphere.visit.model.VisitOutlet;
import com.bnagritech.tradesphere.visit.model.VisitPunch;
import com.bnagritech.tradesphere.visit.repository.VisitFilter;
import com.bnagritech.tradesphere.visit.repository.VisitRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VisitServiceImplTest {

    @Mock
    private VisitRepository visitRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OutletRepository outletRepository;

    @Mock
    private BeatRepository beatRepository;

    @Mock
    private ImageKitService imageKitService;

    @Mock
    private SequenceGenerator sequenceGenerator;

    @InjectMocks
    private VisitServiceImpl visitService;

    @Test
    void planningTwoOutletsCreatesTwoVisitsWithOutletAndBeatDetails() {
        String visitDay = LocalDate.now(ZoneId.of("Asia/Kolkata")).toString();
        User user = User.builder()
                .id("promoter-id")
                .userName("promoter")
                .role(UserRole.PROMOTER)
                .status(UserStatus.ACTIVE)
                .build();
        Outlet firstOutlet = Outlet.builder()
                .id("outlet-id-1")
                .outletCode("OUT-101")
                .name("First Shop")
                .phoneNumber("9876543210")
                .address(OutletAddress.builder().line1("12 Market Road").city("Pune").state("Maharashtra").build())
                .status(OutletStatus.ACTIVE)
                .build();
        Outlet secondOutlet = Outlet.builder()
                .id("outlet-id-2")
                .outletCode("OUT-102")
                .name("Second Shop")
                .phoneNumber("9876543211")
                .status(OutletStatus.ACTIVE)
                .build();

        when(visitRepository.findByUserIdAndVisitDayAndStatusIn(eq(user.getId()), eq(visitDay), anyCollection()))
                .thenReturn(List.of());
        when(beatRepository.findByBeatId("BEAT-1")).thenReturn(Optional.of(Beat.builder()
                .beatId("BEAT-1")
                .assignedUserId(user.getId())
                .outletCodes(List.of("outlet-id-1", "outlet-id-2"))
                .build()));
        when(outletRepository.findById("outlet-id-1")).thenReturn(Optional.of(firstOutlet));
        when(outletRepository.findById("outlet-id-2")).thenReturn(Optional.of(secondOutlet));
        when(sequenceGenerator.next("Visit")).thenReturn(1L, 2L);
        when(visitRepository.save(any(Visit.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PlanVisitsRequest request = PlanVisitsRequest.builder()
                .plannedDate(LocalDate.parse(visitDay))
                .outletCodes(List.of("outlet-id-1", "outlet-id-2"))
                .beatId("BEAT-1")
                .build();

        var response = visitService.planVisits(request, user);

        assertEquals(2, response.getCreated());
        assertEquals(2, response.getVisits().size());
        assertNotEquals(response.getVisits().get(0).getVisitId(), response.getVisits().get(1).getVisitId());
        assertEquals(List.of("OUT-101"), response.getVisits().get(0).getOutletCodes());
        assertEquals(List.of("OUT-102"), response.getVisits().get(1).getOutletCodes());
        assertEquals("BEAT-1", response.getVisits().get(0).getOutletVisits().get(0).getBeatId());
        assertEquals("First Shop", response.getVisits().get(0).getOutletVisits().get(0).getOutletName());
        assertEquals("9876543210", response.getVisits().get(0).getOutletVisits().get(0).getPhoneNumber());
        assertEquals("12 Market Road", response.getVisits().get(0).getOutletVisits().get(0).getAddress().getLine1());
    }

    @Test
    void planningRejectsBeatNotAssignedToCurrentUser() {
        User user = User.builder()
                .id("promoter-id")
                .userName("promoter")
                .role(UserRole.PROMOTER)
                .status(UserStatus.ACTIVE)
                .build();
        when(beatRepository.findByBeatId("BEAT-1")).thenReturn(Optional.of(Beat.builder()
                .beatId("BEAT-1")
                .assignedUserId("another-user")
                .build()));
        PlanVisitsRequest request = PlanVisitsRequest.builder()
                .plannedDate(LocalDate.now(ZoneId.of("Asia/Kolkata")))
                .outletCodes(List.of("OUT-101"))
                .beatId("BEAT-1")
                .build();

        assertThrows(ResponseStatusException.class, () -> visitService.planVisits(request, user));
    }

    @Test
    void checkInRejectsLocationMoreThan100MetersFromOutlet() {
        User user = fieldUser();
        Visit visit = visit(user, VisitStatus.PLANNED);
        Outlet outlet = outlet(new GeoJsonPoint(0.0, 0.0));
        when(visitRepository.findByVisitId("VI000000010")).thenReturn(Optional.of(visit));
        when(visitRepository.findFirstByUserIdAndStatus(user.getId(), VisitStatus.IN_PROGRESS))
                .thenReturn(Optional.empty());
        when(outletRepository.findById("OUT-101")).thenReturn(Optional.empty());
        when(outletRepository.findByOutletCode("OUT-101")).thenReturn(Optional.of(outlet));

        CheckInRequest request = CheckInRequest.builder()
                .visitId("VI000000010")
                .outletCode("OUT-101")
                .location(GeoPointDto.builder().latitude(0.02).longitude(0.02).build())
                .build();

        assertThrows(ResponseStatusException.class, () -> visitService.checkIn(request, user));
    }

    @Test
    void checkInRequiresOutletPhotoWhenLocationIsValid() {
        User user = fieldUser();
        Visit visit = visit(user, VisitStatus.PLANNED);
        Outlet outlet = outlet(new GeoJsonPoint(0.0, 0.0));
        when(visitRepository.findByVisitId("VI000000010")).thenReturn(Optional.of(visit));
        when(visitRepository.findFirstByUserIdAndStatus(user.getId(), VisitStatus.IN_PROGRESS))
                .thenReturn(Optional.empty());
        when(outletRepository.findById("OUT-101")).thenReturn(Optional.empty());
        when(outletRepository.findByOutletCode("OUT-101")).thenReturn(Optional.of(outlet));

        CheckInRequest request = CheckInRequest.builder()
                .visitId("VI000000010")
                .outletCode("OUT-101")
                .location(GeoPointDto.builder().latitude(0.0).longitude(0.0).build())
                .build();

        assertThrows(ResponseStatusException.class, () -> visitService.checkIn(request, user));
    }

    @Test
    void checkInRejectsOutletsWithoutSavedCoordinates() {
        User user = fieldUser();
        Visit visit = visit(user, VisitStatus.PLANNED);
        Outlet outlet = outlet(null);
        when(visitRepository.findByVisitId("VI000000010")).thenReturn(Optional.of(visit));
        when(visitRepository.findFirstByUserIdAndStatus(user.getId(), VisitStatus.IN_PROGRESS))
                .thenReturn(Optional.empty());
        when(outletRepository.findById("OUT-101")).thenReturn(Optional.empty());
        when(outletRepository.findByOutletCode("OUT-101")).thenReturn(Optional.of(outlet));

        CheckInRequest request = CheckInRequest.builder()
                .visitId("VI000000010")
                .outletCode("OUT-101")
                .location(GeoPointDto.builder().latitude(0.0).longitude(0.0).build())
                .build();

        assertThrows(ResponseStatusException.class, () -> visitService.checkIn(request, user));
    }

    @Test
    void checkOutRejectsLocationMoreThan100MetersFromOutlet() {
        User user = fieldUser();
        Visit visit = visit(user, VisitStatus.IN_PROGRESS);
        visit.getOutletVisits().get(0).setStatus(VisitStatus.IN_PROGRESS);
        visit.getOutletVisits().get(0).setCheckIn(VisitPunch.builder()
                .time(Instant.now().minusSeconds(60))
                .location(new GeoJsonPoint(0.0, 0.0))
                .build());
        Outlet outlet = outlet(new GeoJsonPoint(0.0, 0.0));
        when(visitRepository.findByVisitId("VI000000010")).thenReturn(Optional.of(visit));
        when(outletRepository.findById("OUT-101")).thenReturn(Optional.empty());
        when(outletRepository.findByOutletCode("OUT-101")).thenReturn(Optional.of(outlet));

        CheckOutRequest request = CheckOutRequest.builder()
                .location(GeoPointDto.builder().latitude(0.02).longitude(0.02).build())
                .outcome(VisitOutcome.ORDER_TAKEN)
                .build();

        assertThrows(ResponseStatusException.class, () -> visitService.checkOut("VI000000010", request, user));
    }

    @Test
    void visitorAttendanceIsRestrictedToAdministrators() {
        VisitorAttendanceSearchRequest request = VisitorAttendanceSearchRequest.builder().build();
        User nationalHead = User.builder().id("head-id").role(UserRole.NATIONAL_HEAD).build();

        assertThrows(ResponseStatusException.class,
                () -> visitService.getVisitorAttendance(request, fieldUser()));
        assertThrows(ResponseStatusException.class,
                () -> visitService.getVisitorAttendance(request, nationalHead));
    }

    @Test
    void visitorAttendanceIncludesManagerPhotoOutcomeAndRemarks() {
        User visitor = User.builder()
                .id("promoter-id")
                .userName("promoter")
                .fullName("Priya Promoter")
                .managerId("manager-id")
                .role(UserRole.PROMOTER)
                .status(UserStatus.ACTIVE)
                .build();
        User manager = User.builder()
                .id("manager-id")
                .userName("manager")
                .fullName("Mohan Manager")
                .role(UserRole.SALES_OFFICER)
                .status(UserStatus.ACTIVE)
                .build();
        Visit visit = visit(visitor, VisitStatus.COMPLETED);
        VisitOutlet outletVisit = visit.getOutletVisits().get(0);
        outletVisit.setStatus(VisitStatus.COMPLETED);
        outletVisit.setDurationMinutes(42L);
        outletVisit.setOutcome(VisitOutcome.ORDER_TAKEN);
        outletVisit.setOutcomeRemarks("Placed restock order");
        outletVisit.setPictureWithOutlet(MediaRef.builder()
                .fileId("photo-id")
                .url("https://images.example/photo.jpg")
                .build());

        when(visitRepository.searchAttendance(any(VisitFilter.class), eq(0L), eq(25))).thenReturn(List.of(visit));
        when(visitRepository.countAttendance(any(VisitFilter.class))).thenReturn(1L);
        when(userRepository.findAllById(Set.of(visitor.getId()))).thenReturn(List.of(visitor));
        when(userRepository.findAllById(Set.of(manager.getId()))).thenReturn(List.of(manager));

        var response = visitService.getVisitorAttendance(
                VisitorAttendanceSearchRequest.builder().build(),
                User.builder().id("admin-id").role(UserRole.ADMIN).build());

        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
        assertEquals("Priya Promoter", response.getItems().get(0).getVisitorName());
        assertEquals("Mohan Manager", response.getItems().get(0).getManagerName());
        assertEquals("photo-id", response.getItems().get(0).getPhoto().getFileId());
        assertEquals(42L, response.getItems().get(0).getDurationMinutes());
        assertEquals(VisitOutcome.ORDER_TAKEN, response.getItems().get(0).getOutcome());
        assertEquals("Placed restock order", response.getItems().get(0).getRemarks());
    }

    private User fieldUser() {
        return User.builder()
                .id("promoter-id")
                .userName("promoter")
                .role(UserRole.PROMOTER)
                .status(UserStatus.ACTIVE)
                .build();
    }

    private Visit visit(User user, VisitStatus status) {
        return Visit.builder()
                .id("visit-id")
                .visitId("VI000000010")
                .userId(user.getId())
                .visitDay(LocalDate.now(ZoneId.of("Asia/Kolkata")).toString())
                .status(status)
                .outletCodes(List.of("OUT-101"))
                .outletVisits(new ArrayList<>(List.of(VisitOutlet.builder()
                        .outletCode("OUT-101")
                        .status(VisitStatus.PLANNED)
                        .build())))
                .build();
    }

    private Outlet outlet(GeoJsonPoint location) {
        return Outlet.builder()
                .id("outlet-id-1")
                .outletCode("OUT-101")
                .status(OutletStatus.ACTIVE)
                .location(location)
                .build();
    }
}
