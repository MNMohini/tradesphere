package com.bnagritech.tradesphere.auth.service;

public interface OTPService {
    void sendOTP(String phoneNumber);
    boolean verifyOTP(String phoneNumber, String resetPasswordOTP);
}
