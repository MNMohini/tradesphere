package com.bnagritech.tradesphere.auth.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/** One login session on one device. Only a hash of the token is stored, never the token itself. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "refresh_tokens")
public class RefreshToken {

    @Id
    private String id;

    @Indexed
    private String userId;

    @Indexed(unique = true)
    private String tokenHash;

    private String deviceId;
    private String deviceName;
    private String ipAddress;
    private String userAgent;

    private Instant createdAt;

    // MongoDB deletes the document automatically when this moment passes
    @Indexed(expireAfter = "0s")
    private Instant expiresAt;

    private boolean revoked;
    private Instant revokedAt;
}
