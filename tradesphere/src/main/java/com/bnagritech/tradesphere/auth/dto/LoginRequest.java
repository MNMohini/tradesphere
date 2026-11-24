package com.bnagritech.tradesphere.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {

    // Username or registered mobile number
    @NotBlank(message = "username is required")
    @Size(max = 100)
    private String username;

    @NotBlank(message = "password is required")
    @Size(max = 100)
    private String password;

    // Identifies the phone. Needed when the single-device rule is switched on.
    @Size(max = 100)
    private String deviceId;

    @Size(max = 100)
    private String deviceName;

    // Firebase token for push notifications (optional)
    @Size(max = 500)
    private String fcmToken;
}
