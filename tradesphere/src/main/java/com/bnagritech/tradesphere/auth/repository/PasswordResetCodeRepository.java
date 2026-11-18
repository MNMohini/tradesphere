package com.bnagritech.tradesphere.auth.repository;

import com.bnagritech.tradesphere.auth.model.PasswordResetCode;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PasswordResetCodeRepository extends MongoRepository<PasswordResetCode, String> {

    /** The newest code that has not been used yet. */
    Optional<PasswordResetCode> findFirstByUserIdAndUsedFalseOrderByCreatedAtDesc(String userId);

    List<PasswordResetCode> findByUserIdAndUsedFalse(String userId);

    /** How many codes were requested since a moment (to limit "forgot password" requests per user). */
    long countByUserIdAndCreatedAtAfter(String userId, Instant since);
}
