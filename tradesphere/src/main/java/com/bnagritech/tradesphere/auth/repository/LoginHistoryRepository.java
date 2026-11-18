package com.bnagritech.tradesphere.auth.repository;

import com.bnagritech.tradesphere.auth.model.LoginHistory;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface LoginHistoryRepository extends MongoRepository<LoginHistory, String> {

    /** The 50 most recent login attempts of a user. */
    List<LoginHistory> findTop50ByUserIdOrderByAttemptedAtDesc(String userId);
}
