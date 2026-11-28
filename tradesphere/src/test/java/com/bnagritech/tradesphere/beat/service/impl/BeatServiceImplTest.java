package com.bnagritech.tradesphere.beat.service.impl;

import com.bnagritech.tradesphere.auth.model.User;
import com.bnagritech.tradesphere.auth.dto.UserSummaryResponse;
import com.bnagritech.tradesphere.auth.repository.UserRepository;
import com.bnagritech.tradesphere.beat.model.Beat;
import com.bnagritech.tradesphere.beat.repository.BeatRepository;
import com.bnagritech.tradesphere.common.enums.UserRole;
import com.bnagritech.tradesphere.common.enums.UserStatus;
import com.bnagritech.tradesphere.territory.repository.TerritoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BeatServiceImplTest {

    @Mock
    private BeatRepository beatRepository;

    @Mock
    private TerritoryRepository territoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BeatServiceImpl beatService;

    @Test
    void salesOfficerCanAssignBeatToPromoterThroughSupervisorAndAddAnotherAssignee() {
        User salesOfficer = user("sales-officer", "so", UserRole.SALES_OFFICER, null);
        User supervisor = user("supervisor", "sup", UserRole.SUPERVISOR, "so");
        User promoter = user("promoter", "promoter", UserRole.PROMOTER, "supervisor");
        User salesRepresentative = user("sales-rep", "sr", UserRole.SALES_REPRESENTATIVE, "sales-officer");
        stubTeam(supervisor, promoter, salesRepresentative);
        Beat beat = Beat.builder().beatId("BEAT-1").build();
        when(beatRepository.findByBeatId("BEAT-1")).thenReturn(Optional.of(beat));
        when(userRepository.findById("promoter")).thenReturn(Optional.of(promoter));
        when(userRepository.findById("sales-rep")).thenReturn(Optional.of(salesRepresentative));
        when(beatRepository.save(beat)).thenReturn(beat);

        beatService.assignBeat("BEAT-1", "promoter", salesOfficer);
        beatService.assignBeat("BEAT-1", "sales-rep", salesOfficer);

        assertEquals(List.of("promoter", "sales-rep"), beat.getAssignedUserIds());
        assertEquals(List.of("promoter", "sr"), beat.getAssignedUserNames());
        assertEquals("sales-rep", beat.getAssignedUserId());
    }

    @Test
    void supervisorCanAssignBeatToPromoterInTheirTeam() {
        User supervisor = user("supervisor", "supervisor", UserRole.SUPERVISOR, null);
        User promoter = user("promoter", "promoter", UserRole.PROMOTER, "supervisor");
        stubTeam(promoter);
        Beat beat = Beat.builder().beatId("BEAT-2").build();
        when(beatRepository.findByBeatId("BEAT-2")).thenReturn(Optional.of(beat));
        when(userRepository.findById("promoter")).thenReturn(Optional.of(promoter));
        when(beatRepository.save(beat)).thenReturn(beat);

        beatService.assignBeat("BEAT-2", "promoter", supervisor);

        assertEquals(List.of("promoter"), beat.getAssignedUserIds());
    }

    @Test
    void supervisorCannotAssignBeatToSalesRepresentative() {
        User supervisor = user("supervisor", "supervisor", UserRole.SUPERVISOR, null);
        User salesRepresentative = user("sales-rep", "sales-rep", UserRole.SALES_REPRESENTATIVE, "supervisor");
        stubTeam(salesRepresentative);
        Beat beat = Beat.builder().beatId("BEAT-3").build();
        when(beatRepository.findByBeatId("BEAT-3")).thenReturn(Optional.of(beat));
        when(userRepository.findById("sales-rep")).thenReturn(Optional.of(salesRepresentative));

        assertThrows(ResponseStatusException.class,
                () -> beatService.assignBeat("BEAT-3", "sales-rep", supervisor));
        verify(beatRepository, never()).save(beat);
    }

    @Test
    void alreadyAssignedBeatRemainsAssignableToAnotherCurrentTeamMember() {
        User salesOfficer = user("sales-officer", "so", UserRole.SALES_OFFICER, null);
        User supervisor = user("supervisor", "sup", UserRole.SUPERVISOR, "sales-officer");
        User promoter = user("promoter", "promoter", UserRole.PROMOTER, "sup");
        User salesRepresentative = user("sales-rep", "sr", UserRole.SALES_REPRESENTATIVE, "so");
        stubTeam(supervisor, promoter, salesRepresentative);
        Beat beat = Beat.builder()
                .beatId("BEAT-4")
                .assignedUserId("promoter")
                .userName("promoter")
                .assignedUserIds(List.of("promoter"))
                .assignedUserNames(List.of("promoter"))
                .build();
        when(beatRepository.findAll()).thenReturn(List.of(beat));

        List<?> assignableBeats = beatService.getAssignableBeats(salesOfficer);

        assertEquals(1, assignableBeats.size());
    }

    @Test
    void managerCannotAssignBeatAlreadyAssignedOutsideTheirCurrentTeam() {
        User salesOfficer = user("sales-officer", "so", UserRole.SALES_OFFICER, null);
        User salesRepresentative = user("sales-rep", "sr", UserRole.SALES_REPRESENTATIVE, "sales-officer");
        User otherRepresentative = user("other-rep", "other", UserRole.SALES_REPRESENTATIVE, "other-so");
        stubTeam(salesRepresentative, otherRepresentative);
        Beat beat = Beat.builder()
                .beatId("BEAT-5")
                .assignedUserId("other-rep")
                .userName("other")
                .assignedUserIds(List.of("other-rep"))
                .assignedUserNames(List.of("other"))
                .build();
        when(beatRepository.findByBeatId("BEAT-5")).thenReturn(Optional.of(beat));

        assertThrows(ResponseStatusException.class,
                () -> beatService.assignBeat("BEAT-5", "sales-rep", salesOfficer));
        verify(beatRepository, never()).save(beat);
    }

    @Test
    void userCanLoadBeatsAssignedAsOneOfSeveralTeamMembers() {
        User promoter = user("promoter", "promoter", UserRole.PROMOTER, null);
        Beat sharedBeat = Beat.builder()
                .beatId("BEAT-6")
                .assignedUserId("sales-rep")
                .userName("sr")
                .assignedUserIds(List.of("promoter", "sales-rep"))
                .assignedUserNames(List.of("promoter", "sr"))
                .build();
        when(beatRepository.findByAssignedUserIdsContaining("promoter")).thenReturn(List.of(sharedBeat));

        var beats = beatService.getMyBeats(promoter);

        assertEquals(1, beats.size());
        assertEquals(List.of("promoter", "sales-rep"), beats.get(0).getAssignedUserIds());
    }

    @Test
    void assignableUsersIncludeOnlyActiveRolesInCurrentReportingTeam() {
        User salesOfficer = user("sales-officer", "so", UserRole.SALES_OFFICER, null);
        User supervisor = user("supervisor", "sup", UserRole.SUPERVISOR, "so");
        User promoter = user("promoter", "promoter", UserRole.PROMOTER, "supervisor");
        User unrelatedPromoter = user("other-promoter", "other", UserRole.PROMOTER, "other-supervisor");
        stubTeam(supervisor, promoter, unrelatedPromoter);

        List<UserSummaryResponse> users = beatService.getAssignableUsers(salesOfficer);

        assertEquals(2, users.size());
        assertTrue(users.stream().anyMatch(user -> "supervisor".equals(user.getId())));
        assertTrue(users.stream().anyMatch(user -> "promoter".equals(user.getId())));
    }

    private void stubTeam(User... team) {
        when(userRepository.findByManagerIdIn(anyCollection())).thenAnswer(invocation -> {
            Collection<String> references = invocation.getArgument(0);
            return List.of(team).stream()
                    .filter(user -> user.getManagerId() != null
                            && references.stream().anyMatch(reference ->
                            reference.equalsIgnoreCase(user.getManagerId())))
                    .toList();
        });
    }

    private User user(String id, String userName, UserRole role, String managerId) {
        return User.builder()
                .id(id)
                .userName(userName)
                .managerId(managerId)
                .role(role)
                .status(UserStatus.ACTIVE)
                .build();
    }
}
