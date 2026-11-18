package com.bnagritech.tradesphere.auth.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/** A one-time code for "forgot password". Only a hash of the code is stored. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "password_reset_codes")
public class PasswordResetCode {

    @Id
    private String id;

    @Indexed
    private String userId;

    private String codeHash;
    private int attempts;
    private boolean used;
    private Instant createdAt;

    // MongoDB deletes the document automatically when this moment passes
    @Indexed(expireAfter = "0s")
    private Instant expiresAt;
}
