package com.bnagritech.tradesphere.auth.repository;

import com.bnagritech.tradesphere.auth.model.RefreshToken;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends MongoRepository<RefreshToken, String> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /** All sessions of a user that are still usable. */
    List<RefreshToken> findByUserIdAndRevokedFalse(String userId);
}
