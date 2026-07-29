package com.bnagritech.tradesphere.auth.service;

import com.bnagritech.tradesphere.auth.dto.*;

import java.util.List;
public interface AuthService {
    RegisterResponse createUserAccount(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request);

    ResetPasswordResponse resetPassword(ResetPasswordRequest request);

    ChangePasswordResponse changePassword(String userId,
                                          ChangePasswordRequest request);

    RefreshTokenResponse refreshToken(RefreshTokenRequest request);

    UserResponse updateUserStatus(String userId,
                                  UpdateUserStatusRequest request);

    void logout(String token);

    List<UserResponse> getAllUsers();

    UserResponse getUserByUserId(String userId);

}



