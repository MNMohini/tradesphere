package com.bnagritech.tradesphere.auth.controller;

import com.bnagritech.tradesphere.auth.dto.CreateUserRequest;
import com.bnagritech.tradesphere.auth.dto.ImportUsersResponse;
import com.bnagritech.tradesphere.auth.dto.LoginHistoryResponse;
import com.bnagritech.tradesphere.auth.dto.MessageResponse;
import com.bnagritech.tradesphere.auth.dto.MoveUserRequest;
import com.bnagritech.tradesphere.auth.dto.TempPasswordResponse;
import com.bnagritech.tradesphere.auth.dto.UpdateUserRequest;
import com.bnagritech.tradesphere.auth.dto.UpdateUserStatusRequest;
import com.bnagritech.tradesphere.auth.dto.UserResponse;
import com.bnagritech.tradesphere.auth.dto.UserSearchRequest;
import com.bnagritech.tradesphere.auth.dto.UserSummaryResponse;
import com.bnagritech.tradesphere.auth.model.User;
import com.bnagritech.tradesphere.auth.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/** User management. A user can manage only the people below them in the hierarchy. */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // POST /api/users/
    @PostMapping("/create")
    public ResponseEntity<TempPasswordResponse> createUser(
            @Valid @RequestBody CreateUserRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
    }

    // POST /api/users/import    (multipart form with a file field named "file", an .xlsx sheet)
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportUsersResponse> importUsers(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(userService.importUsers(file, currentUser));
    }

    // GET /api/users?keyword=ravi&role=SALES_REPRESENTATIVE&status=ACTIVE&limit=50
    @GetMapping
    public ResponseEntity<List<UserSummaryResponse>> searchUsers(
            @Valid @ModelAttribute UserSearchRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(userService.searchUsers(request, currentUser));
    }

    // GET /api/users/team
    @GetMapping("/team")
    public ResponseEntity<List<UserSummaryResponse>> getMyTeam(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(userService.getMyTeam(currentUser));
    }

    // GET /api/users/{userId}
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable String userId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(userService.getUserById(userId, currentUser));
    }

    // PUT /api/users/{userId}
    @PutMapping("/{userId}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable String userId,
            @Valid @RequestBody UpdateUserRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(userService.updateUser(userId, request, currentUser));
    }

    // PATCH /api/users/{userId}/status    (approve, reject, deactivate, suspend, reactivate)
    @PatchMapping("/{userId}/status")
    public ResponseEntity<UserResponse> updateUserStatus(
            @PathVariable String userId,
            @Valid @RequestBody UpdateUserStatusRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(userService.updateUserStatus(userId, request, currentUser));
    }

    // PATCH /api/users/{userId}/manager
    @PatchMapping("/{userId}/manager")
    public ResponseEntity<UserResponse> moveUser(
            @PathVariable String userId,
            @Valid @RequestBody MoveUserRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(userService.moveUser(userId, request, currentUser));
    }

    // POST /api/users/{userId}/reset-password
    @PostMapping("/{userId}/reset-password")
    public ResponseEntity<TempPasswordResponse> resetUserPassword(
            @PathVariable String userId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(userService.resetUserPassword(userId, currentUser));
    }

    // POST /api/users/{userId}/reset-device
    @PostMapping("/{userId}/reset-device")
    public ResponseEntity<MessageResponse> resetUserDevice(
            @PathVariable String userId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(userService.resetUserDevice(userId, currentUser));
    }

    // GET /api/users/{userId}/login-history
    @GetMapping("/{userId}/login-history")
    public ResponseEntity<List<LoginHistoryResponse>> getLoginHistory(
            @PathVariable String userId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(userService.getLoginHistory(userId, currentUser));
    }
}
