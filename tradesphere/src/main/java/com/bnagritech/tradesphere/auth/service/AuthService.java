package com.bnagritech.tradesphere.auth.service;

import com.bnagritech.tradesphere.auth.dto.*;
import com.bnagritech.tradesphere.common.enums.UserStatus;

import java.util.List;


public interface AuthService {

    //create user account
    RegisterResponse register(RegisterRequest request);
    //login + generate JWT token
    LoginResponse login(LoginRequest request);
    //forgot password
    ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request);
    //reset password
    ResetPasswordResponse resetPassword(ResetPasswordRequest request);
    //change password
    ChangePasswordResponse changePassword(String userId,ChangePasswordRequest request);

    ChangePasswordResponse changePassword(ChangePasswordRequest request);

    //Admin only(activate/deactivated user account)
    UserResponse updateUserStatus(String userId, UpdateUserStatusRequest request);
    //logout user
    void logout(String token);
    //enable / disable user
    UserResponse changeUserStatus(String userId, UserStatus status);

    RegisterResponse changeUserStatus(String userId, Boolean active);

    //admin
    // get all system users
    List<UserResponse> getAllUsers();
    //admin
    //get single User details
    UserResponse getUserById(String userId);
}



