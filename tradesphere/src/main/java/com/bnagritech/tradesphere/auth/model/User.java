package com.bnagritech.tradesphere.auth.model;

import com.bnagritech.tradesphere.common.enums.UserRole;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "users")
public class User {
    @Id
    private String id;

    //Login
    private String userName;
    private String password;

    //contact details
    private String email;
    private String phoneNumber;

    //Personal
    private String firstName;
    private String lastName;
    private String profileImage;

    //mapping
    private String employeeId;
    private String promoterId;
    private String territoryId;

    //Role
    private UserRole role;

    //account status
    private Boolean active;
    @Builder.Default
    private Boolean verified= false;
    @Builder.Default
    private Boolean accountLocked= false;
    @Builder.Default
    private Integer failedLoginAttempts = 0;

    //audit
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updateAt;
    private String createdBy;
    private String updatedBy;



}
