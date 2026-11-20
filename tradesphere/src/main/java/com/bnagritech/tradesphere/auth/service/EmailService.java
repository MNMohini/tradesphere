package com.bnagritech.tradesphere.auth.service;

public interface EmailService {

    boolean sendPasswordResetCode(
            String to,
            String fullName,
            String code,
            int validMinutes);

    boolean sendTemporaryPassword(
            String to,
            String fullName,
            String userName,
            String temporaryPassword);
}
