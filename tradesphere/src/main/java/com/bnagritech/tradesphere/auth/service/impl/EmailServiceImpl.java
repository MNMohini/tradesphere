package com.bnagritech.tradesphere.auth.service.impl;

import com.bnagritech.tradesphere.auth.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Sends plain-text emails through Spring's JavaMailSender. If no mail server is configured
 * (spring.mail.host is empty), nothing is sent and a warning is logged, so the app still starts.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final ObjectProvider<JavaMailSender> mailSender;

    @Value("${app.mail.from:no-reply@localhost}")
    private String from;

    @Override
    public boolean sendPasswordResetCode(String to, String fullName, String code, int validMinutes) {
        String body = "Hello " + safe(fullName) + ",\n\n"
                + "Your password reset code is " + code + ".\n"
                + "It is valid for " + validMinutes + " minutes. If you did not ask for it, ignore this email.\n";
        return send(to, "Your password reset code", body);
    }

    @Override
    public boolean sendTemporaryPassword(String to, String fullName, String userName, String temporaryPassword) {
        String body = "Hello " + safe(fullName) + ",\n\n"
                + "An account has been created for you.\n"
                + "User name: " + userName + "\n"
                + "Temporary password: " + temporaryPassword + "\n\n"
                + "You must change this password the first time you log in.\n";
        return send(to, "Your account details", body);
    }

    private boolean send(String to, String subject, String body) {
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) {
            log.warn("Mail is not configured, so the email \"{}\" to {} was not sent", subject, to);
            return false;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            sender.send(message);
            return true;
        } catch (Exception e) {
            log.error("Could not send the email \"{}\" to {}", subject, to, e);
            return false;
        }
    }

    private String safe(String name) {
        return name == null || name.isBlank() ? "" : name;
    }
}
