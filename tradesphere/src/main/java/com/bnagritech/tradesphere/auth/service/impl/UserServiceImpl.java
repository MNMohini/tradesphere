package com.bnagritech.tradesphere.auth.service.impl;

import com.bnagritech.tradesphere.auth.dto.CreateUserRequest;
import com.bnagritech.tradesphere.auth.dto.ImportRowResult;
import com.bnagritech.tradesphere.auth.dto.ImportUsersResponse;
import com.bnagritech.tradesphere.auth.dto.LoginHistoryResponse;
import com.bnagritech.tradesphere.auth.dto.MessageResponse;
import com.bnagritech.tradesphere.auth.dto.MoveUserRequest;
import com.bnagritech.tradesphere.auth.dto.RegisterRequest;
import com.bnagritech.tradesphere.auth.dto.TempPasswordResponse;
import com.bnagritech.tradesphere.auth.dto.UpdateUserRequest;
import com.bnagritech.tradesphere.auth.dto.UpdateUserStatusRequest;
import com.bnagritech.tradesphere.auth.dto.UserResponse;
import com.bnagritech.tradesphere.auth.dto.UserSearchRequest;
import com.bnagritech.tradesphere.auth.dto.UserSummaryResponse;
import com.bnagritech.tradesphere.auth.model.RefreshToken;
import com.bnagritech.tradesphere.auth.model.User;
import com.bnagritech.tradesphere.auth.repository.LoginHistoryRepository;
import com.bnagritech.tradesphere.auth.repository.RefreshTokenRepository;
import com.bnagritech.tradesphere.auth.repository.UserFilter;
import com.bnagritech.tradesphere.auth.repository.UserRepository;
import com.bnagritech.tradesphere.auth.service.EmailService;
import com.bnagritech.tradesphere.auth.service.UserService;
import com.bnagritech.tradesphere.common.enums.UserRole;
import com.bnagritech.tradesphere.common.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final int DEFAULT_SEARCH_LIMIT = 100;
    private static final int MAX_IMPORT_ROWS = 500;

    // Roles that may see every user
    private static final Set<UserRole> SEE_ALL_ROLES = EnumSet.of(UserRole.ADMIN, UserRole.NATIONAL_HEAD);

    // Roles an employee may pick when registering by themselves
    private static final Set<UserRole> SELF_REGISTER_ROLES =
            EnumSet.of(UserRole.SALES_REPRESENTATIVE, UserRole.PROMOTER);

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final LoginHistoryRepository loginHistoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private final SecureRandom random = new SecureRandom();

    @Value("${app.auth.self-registration:false}")
    private boolean selfRegistrationEnabled = false;

    @Value("${app.auth.admin.username:}")
    private String adminUserName = "";

    @Value("${app.auth.admin.password:}")
    private String adminPassword = "";

    @Value("${app.auth.admin.full-name:System Admin}")
    private String adminFullName = "System Admin";

    @Value("${app.auth.admin.email:}")
    private String adminEmail = "";

    @Value("${app.auth.admin.phone:}")
    private String adminPhone = "";

    /** Runs once the app has started. Creates the first admin only if there is no admin yet. */
    @EventListener(ApplicationReadyEvent.class)
    public void seedFirstAdmin() {
        if (userRepository.existsByRole(UserRole.ADMIN)) {
            return;
        }
        if (adminUserName.isBlank() || adminPassword.isBlank()) {
            log.warn("There is no admin user. Set app.auth.admin.username and app.auth.admin.password "
                    + "(as environment variables) and restart to create the first admin.");
            return;
        }
        if (adminPassword.length() < 8) {
            log.warn("The first admin was not created: app.auth.admin.password must be at least 8 characters.");
            return;
        }

        User admin = User.builder()
                .userName(normalize(adminUserName))
                .passwordHash(passwordEncoder.encode(adminPassword))
                .fullName(adminFullName)
                .email(blankToNull(adminEmail) == null ? null : adminEmail.trim().toLowerCase(Locale.ROOT))
                .phoneNumber(blankToNull(adminPhone))
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .mustChangePassword(true)
                .createdBy("system")
                .build();
        userRepository.save(admin);
        log.info("The first admin '{}' was created. Log in, change the password, then remove "
                + "app.auth.admin.password from your settings.", admin.getUsername());
    }
    // Create / register / import
    @Override
    public TempPasswordResponse createUser(CreateUserRequest request) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication is required");
        }

        String userName = authentication.getName();

        User actor = userRepository.findByUserName(userName)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Authenticated user was not found"
                        )
                );

        return createUserInternal(request, actor);
    }

    private TempPasswordResponse createUserInternal(CreateUserRequest r, User actor) {
        UserRole role = r.getRole();
        List<String> path = new ArrayList<>();
        String managerId = null;

        if (role == UserRole.ADMIN) {
            if (actor.getRole() != UserRole.ADMIN) {
                throw forbidden("Only an admin can create another admin");
            }
        } else {
            if (isBlank(r.getManagerId())) {
                throw badRequest("managerId is required");
            }
            User manager = userRepository.findById(r.getManagerId()).orElseThrow(() -> badRequest("Manager not found"));
            if (manager.getStatus() != UserStatus.ACTIVE) {
                throw badRequest("The manager is not active");
            }
            if (!manager.getRole().allowedChildren().contains(role)) {
                throw badRequest(role + " cannot report to " + manager.getRole());
            }
            // The creator must be the manager, or above the manager (an admin may create anywhere)
            boolean allowed = actor.getRole() == UserRole.ADMIN
                    || actor.getUsername().equals(manager.getUsername())
                    || (manager.getPath() != null && manager.getPath().contains(actor.getUsername()));
            if (!allowed) {
                throw forbidden("You can create users only under yourself or under someone below you");
            }
            managerId = manager.getUsername();
            path.addAll(manager.getPath());
            path.add(manager.getUsername());
        }

        String userName = normalize(r.getUserName());
        String email = r.getEmail() == null || r.getEmail().isBlank() ? null : r.getEmail().trim().toLowerCase(Locale.ROOT);
        requireUnique(userName, r.getPhoneNumber(), email, null);

        String temporaryPassword = generateTemporaryPassword();
        User user = User.builder()
                .userName(userName)
                .passwordHash(passwordEncoder.encode(temporaryPassword))
                .fullName(r.getFullName().trim())
                .email(email)
                .phoneNumber(r.getPhoneNumber())
                .role(role)
                .managerId(managerId)
                .path(path)
                .territoryIds(r.getTerritoryIds() == null ? new ArrayList<>() : new ArrayList<>(r.getTerritoryIds()))
                .status(UserStatus.ACTIVE)
                .mustChangePassword(true)
                .createdBy(actor.getUsername())
                .updatedBy(actor.getUsername())
                .build();

        User saved = saveUnique(user);
        boolean emailSent = email != null
                && emailService.sendTemporaryPassword(email, saved.getFullName(), saved.getUsername(), temporaryPassword);

        return TempPasswordResponse.builder()
                .user(toResponse(saved))
                .temporaryPassword(temporaryPassword)
                .emailSent(emailSent)
                .build();
    }

    @Override
    public MessageResponse selfRegister(RegisterRequest r) {
        if (!selfRegistrationEnabled) {
            throw forbidden("Self-registration is switched off. Ask your manager to create your account.");
        }
        if (!SELF_REGISTER_ROLES.contains(r.getRole())) {
            throw badRequest("Only SALES_REPRESENTATIVE or PROMOTER can register by themselves");
        }

        User manager = userRepository.findByUserName(normalize(r.getManagerUserName()))
                .filter(m -> m.getStatus() == UserStatus.ACTIVE)
                .orElseThrow(() -> badRequest("Manager not found"));
        if (!manager.getRole().allowedChildren().contains(r.getRole())) {
            throw badRequest(r.getRole() + " cannot report to " + manager.getRole());
        }

        String userName = normalize(r.getUserName());
        if (r.getPassword().toLowerCase(Locale.ROOT).contains(userName)) {
            throw badRequest("The password must not contain your user name");
        }
        String email = r.getEmail() == null || r.getEmail().isBlank() ? null : r.getEmail().trim().toLowerCase(Locale.ROOT);
        requireUnique(  userName,
                r.getPhoneNumber(),
                email,
                null);

        List<String> path = new ArrayList<>(manager.getPath());
        path.add(manager.getId());

        saveUnique(User.builder()
                .userName(userName)
                .passwordHash(passwordEncoder.encode(r.getPassword()))
                .fullName(r.getFullName().trim())
                .email(email)
                .phoneNumber(r.getPhoneNumber())
                .role(r.getRole())
                .managerId(manager.getId())
                .path(path)
                .status(UserStatus.PENDING_APPROVAL)
                .mustChangePassword(false)
                .createdBy("self-registration")
                .build());

        log.info("Registration request from '{}' is waiting for approval by '{}'", userName, manager.getUsername());
        return MessageResponse.builder()
                .message("Your request has been sent. Your manager must approve it before you can log in.")
                .build();
    }

    @Override
    public ImportUsersResponse importUsers(MultipartFile file, User actor) {
        if (file == null || file.isEmpty()) {
            throw badRequest("Upload an .xlsx file");
        }
        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase(Locale.ROOT).endsWith(".xlsx")) {
            throw badRequest("Only .xlsx files are supported");
        }

        List<ImportRowResult> results = new ArrayList<>();
        try (InputStream in = file.getInputStream(); Workbook workbook = new XSSFWorkbook(in)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet.getLastRowNum() > MAX_IMPORT_ROWS) {
                throw badRequest("At most " + MAX_IMPORT_ROWS + " rows per file");
            }

            DataFormatter formatter = new DataFormatter();
            Row header = sheet.getRow(0);
            if (header == null) {
                throw badRequest("The first row must contain the column names");
            }
            // Column names are matched ignoring case, spaces and underscores
            Map<String, Integer> columns = new HashMap<>();
            for (Cell cell : header) {
                columns.put(formatter.formatCellValue(cell).replaceAll("[\\s_]", "").toLowerCase(Locale.ROOT),
                        cell.getColumnIndex());
            }
            for (String required : List.of("username", "fullname", "phonenumber", "role", "managerusername")) {
                if (!columns.containsKey(required)) {
                    throw badRequest("Missing column: " + required
                            + ". Required columns: userName, fullName, phoneNumber, role, managerUserName "
                            + "(optional: email, employeeId)");
                }
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                String userName = cellText(row, columns, "username", formatter);
                String fullName = cellText(row, columns, "fullname", formatter);
                String phone = cellText(row, columns, "phonenumber", formatter);
                String roleText = cellText(row, columns, "role", formatter);
                String managerUserName = cellText(row, columns, "managerusername", formatter);
                if (userName == null && fullName == null && phone == null && roleText == null) {
                    continue;   // empty row
                }

                int excelRow = i + 1;
                try {
                    if (userName == null || fullName == null || phone == null || roleText == null || managerUserName == null) {
                        throw badRequest("userName, fullName, phoneNumber, role and managerUserName are required");
                    }
                    UserRole role;
                    try {
                        role = UserRole.valueOf(roleText.trim().toUpperCase(Locale.ROOT).replace(' ', '_'));
                    } catch (IllegalArgumentException e) {
                        throw badRequest("Unknown role: " + roleText);
                    }
                    User manager = userRepository.findByUserName(normalize(managerUserName))
                            .orElseThrow(() -> badRequest("Manager not found: " + managerUserName));

                    CreateUserRequest request = CreateUserRequest.builder()
                            .userName(userName)
                            .fullName(fullName)
                            .phoneNumber(phone)
                            .email(cellText(row, columns, "email", formatter))
                            .role(role)
                            .managerId(manager.getId())
                            .build();
                    if (!userName.matches("^[A-Za-z0-9._-]{3,30}$")) {
                        throw badRequest("userName must be 3 to 30 letters, digits, . _ or -");
                    }
                    if (!phone.matches("^[6-9][0-9]{9}$")) {
                        throw badRequest("phoneNumber must be a 10 digit mobile number");
                    }

                    TempPasswordResponse created = createUserInternal(request, actor);
                    results.add(ImportRowResult.builder()
                            .row(excelRow).userName(normalize(userName)).success(true)
                            .temporaryPassword(created.getTemporaryPassword()).build());
                } catch (ResponseStatusException e) {
                    results.add(ImportRowResult.builder()
                            .row(excelRow).userName(userName).success(false).error(e.getReason()).build());
                } catch (Exception e) {
                    log.error("Unexpected error while importing row {}", excelRow, e);
                    results.add(ImportRowResult.builder()
                            .row(excelRow).userName(userName).success(false).error("Unexpected error").build());
                }
            }
        } catch (IOException e) {
            throw badRequest("The file could not be read. Is it a valid .xlsx file?");
        }

        int created = (int) results.stream().filter(ImportRowResult::isSuccess).count();
        return ImportUsersResponse.builder()
                .total(results.size())
                .created(created)
                .failed(results.size() - created)
                .rows(results)
                .build();
    }
    // Read
    @Override
    public UserResponse getUserById(String userId, User actor) {
        User target = userRepository.findById(userId).orElseThrow(() -> notFound("User not found"));
        if (!canView(actor, target)) {
            throw notFound("User not found");   // hidden users look like missing users
        }
        return toResponse(target);
    }

    @Override
    public List<UserSummaryResponse> searchUsers(UserSearchRequest r, User actor) {
        int limit = r.getLimit() != null ? r.getLimit() : DEFAULT_SEARCH_LIMIT;
        UserFilter filter = new UserFilter(
                SEE_ALL_ROLES.contains(actor.getRole()) ? null : actor.getId(),
                r.getKeyword(), r.getRole(), r.getStatus(), r.getManagerId(), r.getTerritoryId(), limit);
        return userRepository.search(filter).stream().map(this::toSummary).toList();
    }

    @Override
    public List<UserSummaryResponse> getMyTeam(User actor) {
        return userRepository.findByManagerId(actor.getId()).stream().map(this::toSummary).toList();
    }

    @Override
    public List<LoginHistoryResponse> getLoginHistory(String userId, User actor) {
        User target = loadManagedOrSelf(userId, actor);
        return loginHistoryRepository.findTop50ByUserIdOrderByAttemptedAtDesc(target.getId()).stream()
                .map(h -> LoginHistoryResponse.builder()
                        .attemptedAt(h.getAttemptedAt())
                        .userName(h.getUserName())
                        .result(h.getResult())
                        .userAgent(h.getUserAgent())
                        .build())
                .toList();
    }
    // Update / status / move / resets
    @Override
    public UserResponse updateUser(String userId, UpdateUserRequest r, User actor) {
        User target = loadManaged(userId, actor);

        if (r.getFullName() != null) {
            if (r.getFullName().isBlank()) {
                throw badRequest("fullName cannot be blank");
            }
            target.setFullName(r.getFullName().trim());
        }
        String email = r.getEmail() == null || r.getEmail().isBlank() ? null : r.getEmail().trim().toLowerCase(Locale.ROOT);
        requireUnique(null, r.getPhoneNumber(), email, target.getId());

        if (r.getEmail() != null) target.setEmail(email);
        if (r.getPhoneNumber() != null) target.setPhoneNumber(r.getPhoneNumber());
        if (r.getTerritoryIds() != null) target.setTerritoryIds(new ArrayList<>(r.getTerritoryIds()));

        target.setUpdatedBy(actor.getUsername());
        return toResponse(saveUnique(target));
    }

    @Override
    public UserResponse updateUserStatus(String userId, UpdateUserStatusRequest r, User actor) {
        User target = loadManaged(userId, actor);
        UserStatus to = r.getStatus();

        if (to == UserStatus.PENDING_APPROVAL) {
            throw badRequest("A user cannot be set back to PENDING_APPROVAL");
        }
        if (target.getStatus() == to) {
            throw badRequest("The user is already " + to);
        }
        String reason = blankToNull(r.getRemarks());
        if ((to == UserStatus.INACTIVE || to == UserStatus.SUSPENDED) && reason == null) {
            throw badRequest("remarks (the reason) are required when a user is " + to);
        }
        // Stopping a manager would leave their team without anyone to approve their work
        if (to != UserStatus.ACTIVE && userRepository.existsByManagerIdAndStatus(target.getId(), UserStatus.ACTIVE)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This user still has active team members. Move them to another manager first.");
        }

        target.setStatus(to);
        target.setStatusReason(to == UserStatus.ACTIVE ? null : reason);
        target.setUpdatedBy(actor.getId());
        if (to == UserStatus.ACTIVE) {
            target.setFailedLoginAttempts(0);
            target.setLockedUntil(null);
            return toResponse(userRepository.save(target));
        }
        revokeAllSessions(target);   // saves the user
        return toResponse(target);
    }

    @Override
    public UserResponse moveUser(String userId, MoveUserRequest r, User actor) {
        User target = loadManaged(userId, actor);
        if (target.getRole() == UserRole.ADMIN) {
            throw badRequest("An admin has no manager");
        }

        User newManager = userRepository.findById(r.getNewManagerId()).orElseThrow(() -> badRequest("New manager not found"));
        if (newManager.getStatus() != UserStatus.ACTIVE) {
            throw badRequest("The new manager is not active");
        }
        if (!newManager.getRole().allowedChildren().contains(target.getRole())) {
            throw badRequest(target.getRole() + " cannot report to " + newManager.getRole());
        }
        if (newManager.getId().equals(target.getId())
                || (newManager.getPath() != null && newManager.getPath().contains(target.getId()))) {
            throw badRequest("A user cannot be moved under themselves or under their own team");
        }
        // The caller must also be allowed over the new manager (or be the new manager)
        boolean allowed = actor.getRole() == UserRole.ADMIN
                || actor.getId().equals(newManager.getId())
                || (newManager.getPath() != null && newManager.getPath().contains(actor.getId()));
        if (!allowed) {
            throw forbidden("You can move users only under yourself or under someone below you");
        }

        // New ancestors of the user: the new manager's ancestors, then the new manager
        List<String> newPrefix = new ArrayList<>(newManager.getPath());
        newPrefix.add(newManager.getId());
        int oldPrefixLength = target.getPath().size();

        // Everyone below keeps their own part of the path (from the user downwards)
        List<User> changed = new ArrayList<>();
        for (User descendant : userRepository.findByPath(target.getId())) {
            List<String> newPath = new ArrayList<>(newPrefix);
            newPath.addAll(descendant.getPath().subList(oldPrefixLength, descendant.getPath().size()));
            descendant.setPath(newPath);
            descendant.setUpdatedBy(actor.getId());
            changed.add(descendant);
        }
        target.setManagerId(newManager.getId());
        target.setPath(newPrefix);
        target.setUpdatedBy(actor.getUsername());
        changed.add(target);
        userRepository.saveAll(changed);

        log.info("User '{}' and {} team members were moved under '{}'",
                target.getUsername(), changed.size() - 1, newManager.getUsername());
        return toResponse(target);
    }

    @Override
    public TempPasswordResponse resetUserPassword(String userId, User actor) {
        User target = loadManaged(userId, actor);

        String temporaryPassword = generateTemporaryPassword();
        target.setPasswordHash(passwordEncoder.encode(temporaryPassword));
        target.setPasswordChangedAt(Instant.now());
        target.setMustChangePassword(true);
        target.setFailedLoginAttempts(0);
        target.setLockedUntil(null);
        target.setUpdatedBy(actor.getUsername());
        revokeAllSessions(target);   // saves the user

        boolean emailSent = target.getEmail() != null
                && emailService.sendTemporaryPassword(target.getEmail(), target.getFullName(), target.getUsername(), temporaryPassword);

        return TempPasswordResponse.builder()
                .user(toResponse(target))
                .temporaryPassword(temporaryPassword)
                .emailSent(emailSent)
                .build();
    }

    @Override
    public MessageResponse resetUserDevice(String userId, User actor) {
        User target = loadManaged(userId, actor);
        target.setUpdatedBy(actor.getUsername());
        userRepository.save(target);
        return MessageResponse.builder().message("The registered device was cleared. The next login registers a new one.").build();
    }

    @Override
    public void revokeAllSessions(User user) {
        List<RefreshToken> sessions = refreshTokenRepository.findByUserIdAndRevokedFalse(user.getId());
        Instant now = Instant.now();
        sessions.forEach(s -> {
            s.setRevoked(true);
            s.setRevokedAt(now);
        });
        refreshTokenRepository.saveAll(sessions);
        user.setTokensValidFrom(now.truncatedTo(ChronoUnit.SECONDS));
        userRepository.save(user);
    }
    // Rules and permissions

    /** The actor is above the target in the hierarchy (an admin is above everyone). A user never manages themselves. */
    private boolean isManagerOf(User actor, User target) {
        if (actor.getId().equals(target.getId())) {
            return false;
        }
        if (actor.getRole() == UserRole.ADMIN) {
            return true;
        }
        return target.getPath() != null && target.getPath().contains(actor.getId());
    }

    private boolean canView(User actor, User target) {
        return actor.getId().equals(target.getId()) || SEE_ALL_ROLES.contains(actor.getRole()) || isManagerOf(actor, target);
    }

    /** Hidden users look like missing users (404). Managing needs to be above the user. */
    private User loadManaged(String userId, User actor) {
        User target = userRepository.findById(userId).orElseThrow(() -> notFound("User not found"));
        if (!canView(actor, target)) {
            throw notFound("User not found");
        }
        if (!isManagerOf(actor, target)) {
            throw forbidden("You can manage only the people below you");
        }
        return target;
    }

    private User loadManagedOrSelf(String userId, User actor) {
        User target = userRepository.findById(userId).orElseThrow(() -> notFound("User not found"));
        if (actor.getId().equals(target.getId()) || isManagerOf(actor, target)) {
            return target;
        }
        throw notFound("User not found");
    }

    /** excludeId: the user being edited, so they do not clash with themselves (null when creating). */
    private void requireUnique(
            String userName,
            String phoneNumber,
            String email,
            String excludeId) {

        if (userName != null &&
                userRepository.findByUserName(userName)
                        .filter(user -> !Objects.equals(user.getId(), excludeId))
                        .isPresent()) {

            throw conflict("This user name is already taken");
        }

        if (phoneNumber != null &&
                userRepository.findByPhoneNumber(phoneNumber)
                        .filter(user -> !Objects.equals(user.getId(), excludeId))
                        .isPresent()) {

            throw conflict("This phone number is already used by another user");
        }

        if (email != null &&
                userRepository.findByEmail(email)
                        .filter(user -> !Objects.equals(user.getId(), excludeId))
                        .isPresent()) {

            throw conflict("This email is already used by another user");
        }
    }

    /** The unique indexes are the final guard when two requests arrive at the same moment. */
    private User saveUnique(User user) {
        try {
            return userRepository.save(user);
        } catch (DuplicateKeyException e) {
            throw conflict("User name, phone number, email or employee id is already used by another user");
        }
    }

    // =====================================================================
    // Mapping and helpers
    // =====================================================================

    @Override
    public UserResponse toResponse(User u) {
        return UserResponse.builder()
                .id(u.getId())
                .userName(u.getUsername())
                .fullName(u.getFullName())
                .email(u.getEmail())
                .phoneNumber(u.getPhoneNumber())
                .profileImageUrl(u.getProfileImageUrl())
                .role(u.getRole())
                .managerId(u.getManagerId())
                .territoryIds(u.getTerritoryIds())
                .status(u.getStatus())
                .statusReason(u.getStatusReason())
                .mustChangePassword(u.isMustChangePassword())
                .lastLoginAt(u.getLastLoginAt())
                .createdAt(u.getCreatedAt())
                .updatedAt(u.getUpdatedAt())
                .build();
    }

    private UserSummaryResponse toSummary(User u) {
        return UserSummaryResponse.builder()
                .id(u.getId())
                .userName(u.getUsername())
                .fullName(u.getFullName())
                .phoneNumber(u.getPhoneNumber())
                .role(u.getRole())
                .managerId(u.getManagerId())
                .status(u.getStatus())
                .lastLoginAt(u.getLastLoginAt())
                .build();
    }

    /** 10 characters with a capital letter, a small letter and a digit (no look-alike characters). */
    private String generateTemporaryPassword() {
        String upper = "ABCDEFGHJKLMNPQRSTUVWXYZ";
        String lower = "abcdefghijkmnopqrstuvwxyz";
        String digits = "23456789";
        String all = upper + lower + digits;

        List<Character> chars = new ArrayList<>();
        chars.add(upper.charAt(random.nextInt(upper.length())));
        chars.add(lower.charAt(random.nextInt(lower.length())));
        chars.add(digits.charAt(random.nextInt(digits.length())));
        for (int i = 0; i < 7; i++) {
            chars.add(all.charAt(random.nextInt(all.length())));
        }
        Collections.shuffle(chars, random);

        StringBuilder password = new StringBuilder();
        chars.forEach(password::append);
        return password.toString();
    }

    private String cellText(Row row, Map<String, Integer> columns, String column, DataFormatter formatter) {
        Integer index = columns.get(column);
        if (index == null) {
            return null;
        }
        Cell cell = row.getCell(index);
        return cell == null ? null : blankToNull(formatter.formatCellValue(cell));
    }

    private String normalize(String userName) {
        return userName.trim().toLowerCase(Locale.ROOT);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String blankToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }

    private ResponseStatusException forbidden(String message) {
        return new ResponseStatusException(HttpStatus.FORBIDDEN, message);
    }

    private ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }
}
