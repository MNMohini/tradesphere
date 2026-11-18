package com.bnagritech.tradesphere.auth.model;

import com.bnagritech.tradesphere.common.enums.LoginResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/** One login attempt, successful or not. Kept for 180 days. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "login_history")
@CompoundIndex(name = "user_time", def = "{'userId': 1, 'attemptedAt': -1}")
public class LoginHistory {

    @Id
    private String id;

    private String userId;            // null when the username did not match any user
    private String userName;          // what was typed
    private LoginResult result;
    private String userAgent;

    @Indexed(expireAfter = "15_552_000")
    private Instant attemptedAt;
}
