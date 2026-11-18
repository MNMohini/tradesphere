package com.bnagritech.tradesphere.auth.dto;

import com.bnagritech.tradesphere.common.enums.LoginResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginHistoryResponse {

    private Instant attemptedAt;
    private String userName;
    private LoginResult result;
    private String ipAddress;
    private String deviceId;
    private String userAgent;
}
