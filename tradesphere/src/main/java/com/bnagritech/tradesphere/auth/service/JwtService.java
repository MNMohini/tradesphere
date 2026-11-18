package com.bnagritech.tradesphere.auth.service;

import com.bnagritech.tradesphere.auth.model.User;

import java.time.Instant;
import java.util.Optional;

/** Creates and reads the short-lived access tokens sent in the "Authorization: Bearer ..." header. */
public interface JwtService {

    /** What the filter needs from a valid token. */
    record TokenData(String userId, Instant issuedAt) {
    }

    String generateAccessToken(User user);

    /** Returns the data of a valid, unexpired token, or empty if the token is invalid in any way. */
    Optional<TokenData> parse(String token);

    long getAccessTokenSeconds();
}
