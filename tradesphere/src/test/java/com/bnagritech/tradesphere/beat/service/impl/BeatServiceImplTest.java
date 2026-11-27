package com.bnagritech.tradesphere.beat.service.impl;

import com.bnagritech.tradesphere.auth.model.User;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    void salesOfficerCanAssignBeatToPromoterInTheirTeam() {
        User salesOfficer = user("sales-officer", "so", UserRole.SALES_OFFICER, List.of());
        User promoter = user("promoter", "promoter", UserRole.PROMOTER, List.of("sales-officer"));
        Beat beat = Beat.builder().beatId("BEAT-1").build();
        when(beatRepository.findByBeatId("BEAT-1")).thenReturn(Optional.of(beat));
        when(userRepository.findByPath("sales-officer")).thenReturn(List.of(promoter));
        when(userRepository.findById("promoter")).thenReturn(Optional.of(promoter));
        when(beatRepository.save(beat)).thenReturn(beat);

        beatService.assignBeat("BEAT-1", "promoter", salesOfficer);

        assertEquals("promoter", beat.getAssignedUserId());
        assertEquals("promoter", beat.getUserName());
    }

    @Test
    void supervisorCanAssignBeatToPromoterInTheirTeam() {
        User supervisor = user("supervisor", "supervisor", UserRole.SUPERVISOR, List.of());
        User promoter = user("promoter", "promoter", UserRole.PROMOTER, List.of("supervisor"));
        Beat beat = Beat.builder().beatId("BEAT-2").build();
        when(beatRepository.findByBeatId("BEAT-2")).thenReturn(Optional.of(beat));
        when(userRepository.findByPath("supervisor")).thenReturn(List.of(promoter));
        when(userRepository.findById("promoter")).thenReturn(Optional.of(promoter));
        when(beatRepository.save(beat)).thenReturn(beat);

        beatService.assignBeat("BEAT-2", "promoter", supervisor);

        assertEquals("promoter", beat.getAssignedUserId());
    }

    @Test
    void supervisorCannotAssignBeatToSalesRepresentative() {
        User supervisor = user("supervisor", "supervisor", UserRole.SUPERVISOR, List.of());
        User salesRepresentative = user(
                "sales-rep", "sales-rep", UserRole.SALES_REPRESENTATIVE, List.of("supervisor"));
        Beat beat = Beat.builder().beatId("BEAT-3").build();
        when(beatRepository.findByBeatId("BEAT-3")).thenReturn(Optional.of(beat));
        when(userRepository.findByPath("supervisor")).thenReturn(List.of(salesRepresentative));
        when(userRepository.findById("sales-rep")).thenReturn(Optional.of(salesRepresentative));

        assertThrows(ResponseStatusException.class,
                () -> beatService.assignBeat("BEAT-3", "sales-rep", supervisor));
        verify(beatRepository, never()).save(beat);
    }

    private User user(String id, String userName, UserRole role, List<String> path) {
        return User.builder()
                .id(id)
                .userName(userName)
                .role(role)
                .status(UserStatus.ACTIVE)
                .path(path)
                .build();
    }
}
