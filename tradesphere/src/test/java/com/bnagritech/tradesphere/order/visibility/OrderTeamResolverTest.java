package com.bnagritech.tradesphere.order.visibility;

import com.bnagritech.tradesphere.auth.model.User;
import com.bnagritech.tradesphere.auth.repository.UserRepository;
import com.bnagritech.tradesphere.common.enums.UserRole;
import com.bnagritech.tradesphere.order.model.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderTeamResolverTest {

    @Mock
    private UserRepository userRepository;

    private final List<User> users = new ArrayList<>();
    private OrderTeamResolver resolver;
    private User nationalHead;
    private User firstRsm;
    private User secondRsm;
    private User asm;
    private User secondAsm;
    private User so;
    private User otherSo;
    private User sr;
    private User supervisor;
    private User promoter;

    @BeforeEach
    void setUp() {
        nationalHead = user("nh-id", "national", null, UserRole.NATIONAL_HEAD);
        firstRsm = user("rsm-one-id", "rsm-one", "national", UserRole.REGIONAL_MANAGER);
        secondRsm = user("rsm-two-id", "rsm-two", "national", UserRole.REGIONAL_MANAGER);
        asm = user("asm-id", "asm", firstRsm.getId(), UserRole.AREA_MANAGER);
        secondAsm = user("second-asm-id", "second-asm", secondRsm.getId(), UserRole.AREA_MANAGER);
        so = user("so-id", "so", asm.getId(), UserRole.SALES_OFFICER);
        otherSo = user("other-so-id", "other-so", secondAsm.getId(), UserRole.SALES_OFFICER);
        sr = user("sr-id", "sr", so.getUsername(), UserRole.SALES_REPRESENTATIVE);
        supervisor = user("supervisor-id", "supervisor", so.getUsername(), UserRole.SUPERVISOR);
        promoter = user("promoter-id", "promoter", supervisor.getId(), UserRole.PROMOTER);

        users.addAll(List.of(
                nationalHead, firstRsm, secondRsm, asm, secondAsm, so, otherSo, sr, supervisor, promoter));
        when(userRepository.findByManagerIdIn(anyCollection())).thenAnswer(invocation -> {
            Set<?> managerReferences = Set.copyOf((java.util.Collection<?>) invocation.getArgument(0));
            return users.stream()
                    .filter(user -> user.getManagerId() != null
                            && managerReferences.contains(user.getManagerId()))
                    .toList();
        });
        lenient().when(userRepository.findById(anyString())).thenAnswer(invocation ->
                users.stream()
                        .filter(user -> user.getId().equals(invocation.getArgument(0)))
                        .findFirst());
        lenient().when(userRepository.findByUserName(anyString())).thenAnswer(invocation ->
                users.stream()
                        .filter(user -> user.getUsername().equals(invocation.getArgument(0)))
                        .findFirst());
        resolver = new OrderTeamResolver(userRepository);
    }

    @Test
    void resolvesCurrentOrderDescendantsAndExcludesPromoters() {
        OrderTeamResolver.TeamTree rsmTree = resolver.resolve(firstRsm);
        OrderTeamResolver.TeamTree srTree = resolver.resolve(sr);
        OrderTeamResolver.TeamTree supervisorTree = resolver.resolve(supervisor);

        assertTrue(rsmTree.contains(so.getId()));
        assertTrue(rsmTree.contains(sr.getId()));
        assertFalse(rsmTree.contains(secondRsm.getId()));
        assertFalse(rsmTree.contains(otherSo.getId()));
        assertFalse(rsmTree.contains(promoter.getId()));
        assertFalse(supervisorTree.contains(promoter.getId()));
        assertFalse(srTree.contains(promoter.getId()));
        assertFalse(srTree.contains(so.getId()));
    }

    @Test
    void currentManagerChangesMoveExistingOrdersToTheNewTeam() {
        Order oldTeamOrder = Order.builder().createdBy(sr.getId()).userPath(List.of("rsm-one-id")).build();

        assertTrue(resolver.canViewOrder(firstRsm, oldTeamOrder));
        assertFalse(resolver.canViewOrder(secondRsm, oldTeamOrder));

        sr.setManagerId(otherSo.getUsername());

        assertFalse(resolver.canViewOrder(firstRsm, oldTeamOrder));
        assertTrue(resolver.canViewOrder(secondRsm, oldTeamOrder));
    }

    @Test
    void upperManagersSeeJuniorOrdersButJuniorCannotSeeManagerOrders() {
        Order srOrder = Order.builder().createdBy(sr.getId()).build();
        Order soOrder = Order.builder().createdBy(so.getId()).build();

        assertTrue(resolver.canViewOrder(nationalHead, srOrder));
        assertTrue(resolver.canViewOrder(firstRsm, srOrder));
        assertTrue(resolver.canViewOrder(asm, srOrder));
        assertTrue(resolver.canViewOrder(so, srOrder));
        assertFalse(resolver.canViewOrder(sr, soOrder));
    }

    @Test
    void supportsLegacyOrdersStoredWithTheCreatorsUsername() {
        Order legacyOrder = Order.builder().createdBy(sr.getUsername()).build();

        assertTrue(resolver.canViewOrder(firstRsm, legacyOrder));
        assertFalse(resolver.canViewOrder(secondRsm, legacyOrder));
    }

    @Test
    void requestedMemberMustBeInsideTheAuthenticatedUsersTeam() {
        OrderTeamResolver.TeamTree tree = resolver.resolve(firstRsm);

        assertTrue(tree.findMember(so.getId()).isPresent());
        assertTrue(tree.findMember(firstRsm.getId()).isPresent());
        assertFalse(tree.findMember(secondRsm.getId()).isPresent());
    }

    @Test
    void promoterCannotBePlacedUnderSalesRepresentativeForVisibility() {
        promoter.setManagerId(sr.getUsername());

        assertFalse(resolver.resolve(sr).contains(promoter.getId()));
        assertFalse(resolver.canViewOrder(promoter, Order.builder().createdBy(promoter.getId()).build()));
    }

    private User user(String id, String username, String managerId, UserRole role) {
        return User.builder()
                .id(id)
                .userName(username)
                .managerId(managerId)
                .role(role)
                .fullName(username)
                .build();
    }
}
