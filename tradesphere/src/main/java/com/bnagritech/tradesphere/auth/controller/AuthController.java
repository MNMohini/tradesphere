package com.bnagritech.tradesphere.auth.controller;

import com.bnagritech.tradesphere.auth.dto.AuthResponse;
import com.bnagritech.tradesphere.auth.dto.ChangePasswordRequest;
import com.bnagritech.tradesphere.auth.dto.ForgotPasswordRequest;
import com.bnagritech.tradesphere.auth.dto.LoginRequest;
import com.bnagritech.tradesphere.auth.dto.MessageResponse;
import com.bnagritech.tradesphere.auth.dto.RefreshTokenRequest;
import com.bnagritech.tradesphere.auth.dto.RegisterRequest;
import com.bnagritech.tradesphere.auth.dto.ResetPasswordRequest;
import com.bnagritech.tradesphere.auth.dto.UpdateProfileRequest;
import com.bnagritech.tradesphere.auth.dto.UserResponse;
import com.bnagritech.tradesphere.auth.model.User;
import com.bnagritech.tradesphere.auth.service.AuthService;
import com.bnagritech.tradesphere.auth.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Login, sessions, passwords and "my profile". The first six endpoints are public (no token needed).
 * Behind a proxy or load balancer, set server.forward-headers-strategy=framework so the real client address is used.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    // POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest http) {
        return ResponseEntity.ok(authService.login(request, clientIp(http), userAgent(http)));
    }

    // POST /api/auth/refresh
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request, HttpServletRequest http) {
        return ResponseEntity.ok(authService.refresh(request, clientIp(http), userAgent(http)));
    }

    // POST /api/auth/forgot-password
    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request, HttpServletRequest http) {
        return ResponseEntity.ok(authService.forgotPassword(request, clientIp(http)));
    }

    // POST /api/auth/reset-password
    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(authService.resetPassword(request));
    }

    // POST /api/auth/register   (only when app.auth.self-registration=true)
    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.selfRegister(request));
    }

    // POST /api/auth/logout   (ends the session of the given refresh token)
    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(
            @Valid @RequestBody RefreshTokenRequest request,
            @AuthenticationPrincipal User currentUser) {
        authService.logout(request, currentUser);
        return ResponseEntity.ok(MessageResponse.builder().message("Logged out").build());
    }

    // POST /api/auth/logout-all   (ends every session on every device)
    @PostMapping("/logout-all")
    public ResponseEntity<MessageResponse> logoutAll(@AuthenticationPrincipal User currentUser) {
        authService.logoutAll(currentUser);
        return ResponseEntity.ok(MessageResponse.builder().message("Logged out from all devices").build());
    }

    // POST /api/auth/change-password
    @PostMapping("/change-password")
    public ResponseEntity<AuthResponse> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest http) {
        return ResponseEntity.ok(authService.changePassword(request, currentUser, clientIp(http), userAgent(http)));
    }

    // GET /api/auth/me
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(authService.getMyProfile(currentUser));
    }

    // PUT /api/auth/me
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateMyProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(authService.updateMyProfile(request, currentUser));
    }

    private String clientIp(HttpServletRequest http) {
        return http.getRemoteAddr();
    }

    private String userAgent(HttpServletRequest http) {
        String agent = http.getHeader("User-Agent");
        return agent != null && agent.length() > 300 ? agent.substring(0, 300) : agent;
    }
}
