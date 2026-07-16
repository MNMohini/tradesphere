package com.bnagritech.tradesphere.auth.service;

public interface OTPService {
    void sendOTP(String phoneNumber);
    boolean verifyOtp(String phoneNumber, String resetPasswordOTP);
}
