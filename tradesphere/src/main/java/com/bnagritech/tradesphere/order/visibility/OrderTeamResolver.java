package com.bnagritech.tradesphere.order.visibility;

import com.bnagritech.tradesphere.auth.model.User;
import com.bnagritech.tradesphere.auth.repository.UserRepository;
import com.bnagritech.tradesphere.order.model.Order;
import com.bnagritech.tradesphere.common.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class OrderTeamResolver {

    private final UserRepository userRepository;

    public TeamTree resolve(User root) {
        Map<String, List<User>> reportsByManagerReference = new LinkedHashMap<>();
        Map<String, User> descendantsById = new LinkedHashMap<>();
        Set<String> seenIds = new LinkedHashSet<>();
        seenIds.add(root.getId());

        List<User> parents = List.of(root);
        while (!parents.isEmpty()) {
            List<String> managerReferences = parents.stream()
                    .flatMap(user -> identityValues(user).stream())
                    .distinct()
                    .toList();
            if (managerReferences.isEmpty()) {
                break;
            }

            List<User> reports = userRepository.findByManagerIdIn(managerReferences);
            Map<String, User> parentsByReference = new LinkedHashMap<>();
            parents.forEach(parent -> identityValues(parent).forEach(
                    reference -> parentsByReference.put(reference, parent)));
            List<User> nextLevel = new ArrayList<>();
            for (User report : reports) {
                User manager = parentsByReference.get(report.getManagerId());
                if (manager == null || !isAllowedChild(manager, report)
                        || report.getId() == null || !seenIds.add(report.getId())) {
                    continue;
                }
                descendantsById.put(report.getId(), report);
                if (report.getManagerId() != null) {
                    reportsByManagerReference
                            .computeIfAbsent(report.getManagerId(), ignored -> new ArrayList<>())
                            .add(report);
                }
                nextLevel.add(report);
            }
            parents = nextLevel;
        }

        return new TeamTree(root, descendantsById, reportsByManagerReference);
    }

    private boolean isAllowedChild(User manager, User report) {
        return manager.getRole() != null
                && report.getRole() != null
                && report.getRole() != UserRole.PROMOTER
                && manager.getRole().allowedChildren().contains(report.getRole());
    }

    public Optional<User> findUser(String reference) {
        if (reference == null || reference.isBlank()) {
            return Optional.empty();
        }
        String normalized = reference.trim();
        return userRepository.findById(normalized)
                .or(() -> userRepository.findByUserName(normalized.toLowerCase(java.util.Locale.ROOT)));
    }

    public List<User> findUsers(Collection<String> references) {
        List<String> requested = references.stream()
                .filter(Objects::nonNull)
                .filter(reference -> !reference.isBlank())
                .distinct()
                .toList();
        List<String> distinct = Stream.concat(
                        requested.stream(),
                        requested.stream().map(reference -> reference.toLowerCase(java.util.Locale.ROOT)))
                .distinct()
                .toList();
        if (distinct.isEmpty()) {
            return List.of();
        }

        Map<String, User> usersById = new LinkedHashMap<>();
        userRepository.findAllById(distinct).forEach(user -> usersById.put(user.getId(), user));
        userRepository.findByUserNameIn(distinct).forEach(user -> usersById.put(user.getId(), user));
        return List.copyOf(usersById.values());
    }

    public List<String> identityValues(User user) {
        return Stream.of(user.getId(), user.getUsername())
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    public boolean canViewOrder(User viewer, Order order) {
        if (viewer == null || viewer.getRole() == UserRole.PROMOTER) {
            return false;
        }
        if (viewer.getRole() == UserRole.ADMIN) {
            return true;
        }
        Optional<User> creator = findUser(order.getCreatedBy());
        if (creator.isEmpty()) {
            return false;
        }
        if (Objects.equals(creator.get().getId(), viewer.getId())) {
            return true;
        }
        return resolve(viewer).contains(creator.get().getId());
    }

    public static final class TeamTree {
        private final User root;
        private final Map<String, User> descendantsById;
        private final Map<String, List<User>> reportsByManagerReference;

        private TeamTree(
                User root,
                Map<String, User> descendantsById,
                Map<String, List<User>> reportsByManagerReference) {
            this.root = root;
            this.descendantsById = descendantsById;
            this.reportsByManagerReference = reportsByManagerReference;
        }

        public User root() {
            return root;
        }

        public List<User> descendants() {
            return List.copyOf(descendantsById.values());
        }

        public Optional<User> findMember(String userId) {
            if (Objects.equals(root.getId(), userId)) {
                return Optional.of(root);
            }
            return Optional.ofNullable(descendantsById.get(userId));
        }

        public boolean contains(String userId) {
            return Objects.equals(root.getId(), userId) || descendantsById.containsKey(userId);
        }

        public List<User> directReports(User manager) {
            Map<String, User> reports = new LinkedHashMap<>();
            identityValues(manager).forEach(reference ->
                    reportsByManagerReference.getOrDefault(reference, List.of())
                            .forEach(user -> reports.putIfAbsent(user.getId(), user)));
            return List.copyOf(reports.values());
        }

        public List<User> subtree(User member) {
            Map<String, User> result = new LinkedHashMap<>();
            ArrayDeque<User> pending = new ArrayDeque<>();
            pending.add(member);
            while (!pending.isEmpty()) {
                User current = pending.removeFirst();
                if (result.putIfAbsent(current.getId(), current) != null) {
                    continue;
                }
                pending.addAll(directReports(current));
            }
            return List.copyOf(result.values());
        }

        private static List<String> identityValues(User user) {
            return Stream.of(user.getId(), user.getUsername())
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
        }
    }
}
