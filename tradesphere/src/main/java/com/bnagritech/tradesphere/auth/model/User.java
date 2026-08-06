package com.bnagritech.tradesphere.auth.model;

import com.bnagritech.tradesphere.common.enums.UserRole;
import com.bnagritech.tradesphere.common.enums.UserStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "users")
public class User implements UserDetails {
    @Id
    private String id;
    //Login
    @NotBlank
    private String userName;
    @NotBlank
    private String password;
    //mapping
    private String employeeId;
    private String employeeName;
    private String email;
    private String phoneNumber;
    private String profileImageUrl;
    //Role
    private UserRole role;
    private Set<String> permissions;
    private UserStatus status = UserStatus.ACTIVE;
    @Builder.Default
    private Boolean accountLocked= false;
    @Builder.Default
    private Integer failedLoginAttempts = 0;
    @Builder.Default
    private Boolean enabled = true;
    //audit
    private  LocalDateTime lastLoginAt;
    private LocalDateTime passwordChangeAt;
    private LocalDateTime refreshTokenExpiry;
    private String refreshToken;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updateAt;
    @CreatedBy
    private String createdBy;
    @LastModifiedBy
    private String updatedBy;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if(permissions != null && !permissions.isEmpty()){
            return permissions.stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList();
        }
        return List.of( new SimpleGrantedAuthority("ROLE_" +role.name()) );
    }

    @Override
    public String getUsername() {
        return userName;
    }
}
