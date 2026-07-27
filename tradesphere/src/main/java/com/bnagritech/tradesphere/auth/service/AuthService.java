package com.bnagritech.tradesphere.auth.service;

import com.bnagritech.tradesphere.auth.dto.*;

import java.util.List;
public interface AuthService {
    //create user account
    RegisterResponse createUserAccount(RegisterRequest request);
    //login + generate JWT token
    LoginResponse login(LoginRequest request);
    //forgot password
    ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request);
    VerifyOTPResponse verifyOTP(VerifyOTPRequest request);
    ResetPasswordResponse resetPassword(ResetPasswordRequest request);
    //change password
    ChangePasswordResponse changePassword(ChangePasswordRequest request);
    //Admin only(activate/deactivated user account)
    UserResponse updateUserStatus(String userId, UpdateUserStatusRequest request);
    //logout user
    void logout(String token);
    //admin
    // get all system users
    List<UserResponse> getAllUsers();
    //get single User details
    UserResponse getUserByUserId(String userId);

}



