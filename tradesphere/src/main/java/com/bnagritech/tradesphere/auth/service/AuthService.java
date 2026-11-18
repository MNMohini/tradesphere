package com.bnagritech.tradesphere.auth.service;

import com.bnagritech.tradesphere.auth.dto.AuthResponse;
import com.bnagritech.tradesphere.auth.dto.ChangePasswordRequest;
import com.bnagritech.tradesphere.auth.dto.ForgotPasswordRequest;
import com.bnagritech.tradesphere.auth.dto.LoginRequest;
import com.bnagritech.tradesphere.auth.dto.MessageResponse;
import com.bnagritech.tradesphere.auth.dto.RefreshTokenRequest;
import com.bnagritech.tradesphere.auth.dto.ResetPasswordRequest;
import com.bnagritech.tradesphere.auth.dto.UpdateProfileRequest;
import com.bnagritech.tradesphere.auth.dto.UserResponse;
import com.bnagritech.tradesphere.auth.model.User;

/** Login, sessions, passwords and the logged-in user's own profile. */
public interface AuthService {

    /** Checks the password, applies the lock and device rules, and starts a session. */
    AuthResponse login(LoginRequest request, String ipAddress, String userAgent);

    /** Swaps a refresh token for a new access token and a NEW refresh token (the old one stops working). */
    AuthResponse refresh(RefreshTokenRequest request, String ipAddress, String userAgent);

    /** Ends the session that owns this refresh token. */
    void logout(RefreshTokenRequest request, User currentUser);

    /** Ends every session of the user on every device. */
    void logoutAll(User currentUser);

    /** Changes the password, ends all other sessions and returns a fresh session for this device. */
    AuthResponse changePassword(ChangePasswordRequest request, User currentUser, String ipAddress, String userAgent);

    /** Emails a one-time code. The answer is always the same, so nobody can find out which accounts exist. */
    MessageResponse forgotPassword(ForgotPasswordRequest request, String ipAddress);

    /** Sets a new password using the emailed code. */
    MessageResponse resetPassword(ResetPasswordRequest request);

    UserResponse getMyProfile(User currentUser);

    UserResponse updateMyProfile(UpdateProfileRequest request, User currentUser);
}
