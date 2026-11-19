package com.bnagritech.tradesphere.auth.model;

import com.bnagritech.tradesphere.common.enums.UserRole;
import com.bnagritech.tradesphere.common.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An employee account. This class replaces your earlier User entity. It is also the Spring Security
 * principal, so controllers can receive it with @AuthenticationPrincipal.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User implements UserDetails {

    @Id
    private String id;

    @Version
    private Long version;

    // ---- Login ----
    @Indexed(unique = true)
    private String userName;             // stored in lower case

    private String passwordHash;         // BCrypt hash, never returned by any API

    // ---- Employee details ----
    private String fullName;

    @Indexed(unique = true, sparse = true)
    private String email;

    @Indexed(unique = true, sparse = true)
    private String phoneNumber;

    private String profileImageUrl;

    // ---- Role and permissions ----
    private UserRole role;

    @Builder.Default
    private Set<String> permissions = new HashSet<>();

    // ---- Hierarchy ----
    @Indexed
    private String managerId;

    // Ids of everyone above this user, from the top down. Other modules copy it to decide who can see what.
    @Indexed
    @Builder.Default
    private List<String> path = new ArrayList<>();

    @Builder.Default
    private List<String> territoryIds = new ArrayList<>();

    // ---- Account state ----
    private UserStatus status;
    private String statusReason;

    @Builder.Default
    private int failedLoginAttempts = 0;

    private Instant lockedUntil;

    // true after an admin-created account or a password reset: the user must choose a new password
    private boolean mustChangePassword;

    // Access tokens issued before this moment are rejected (set on logout-all, password change, deactivation)
    private Instant tokensValidFrom;

    // ---- Device (optional single-device rule, push notifications) ----
    private String deviceId;
    private String deviceName;
    private String fcmToken;

    // ---- Audit ----
    private Instant lastLoginAt;
    private Instant passwordChangedAt;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    private String createdBy;
    private String updatedBy;

    // ---------------- UserDetails ----------------

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (role != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.name()));
        }
        if (permissions != null) {
            permissions.forEach(p -> authorities.add(new SimpleGrantedAuthority(p)));
        }
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return userName;
    }

    @Override
    public boolean isAccountNonLocked() {
        return lockedUntil == null || lockedUntil.isBefore(Instant.now());
    }

    @Override
    public boolean isEnabled() {
        return status == UserStatus.ACTIVE;
    }
}
