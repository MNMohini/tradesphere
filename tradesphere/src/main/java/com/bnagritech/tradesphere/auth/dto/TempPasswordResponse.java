package com.bnagritech.tradesphere.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Returned when a user is created or a manager resets a password.
 * temporaryPassword is shown only this once; the user must change it at first login.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TempPasswordResponse {

    private UserResponse user;
    private String temporaryPassword;

    // true if the password was also emailed to the user
    private boolean emailSent;
}
