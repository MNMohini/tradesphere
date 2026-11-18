package com.bnagritech.tradesphere.auth.service;

/** Sends the emails of the auth module. Every method returns true if the email was handed to the mail server. */
public interface EmailService {

    boolean sendPasswordResetCode(String to, String fullName, String code, int validMinutes);

    boolean sendTemporaryPassword(String to, String fullName, String userName, String temporaryPassword);
}
