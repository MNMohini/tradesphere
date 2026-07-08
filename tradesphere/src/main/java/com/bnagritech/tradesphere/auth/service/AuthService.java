package com.bnagritech.tradesphere.auth.service;

import com.bnagritech.tradesphere.auth.dto.*;


public interface AuthService {

    //create user account
    RegisterResponse register(
            RegisterRequest request
    );
    //login + generate JWT token
    LoginResponse login(
            LoginRequest request
    );
    //forgot password
    ForgotPasswordResponse forgotPassword(
            ForgotPasswordRequest request
    );
    //reset password
    ResetPasswordResponse resetPassword(
            ResetPasswordRequest request
    );
    //logout user
    void logout(
            String token
    );
    //enable / disable user
    RegisterResponse changeUserStatus(
            String userId,
            Boolean active
    );
    //lock / unlock account
    RegisterResponse changeAccountLockStatus(
            String userId,
            Boolean locked
    );

}



