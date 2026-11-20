package com.bnagritech.tradesphere.auth.service.impl;

import com.bnagritech.tradesphere.auth.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Override
    public boolean sendPasswordResetCode(
            String to,
            String fullName,
            String code,
            int validMinutes) {

        String subject = "TradeSphere Password Reset Code";

        String body = """
                Hello %s,

                We received a request to reset your TradeSphere password.

                Your password reset verification code is:

                %s

                This code is valid for %d minutes.

                Please do not share this code with anyone.

                If you did not request a password reset, you can safely ignore this email.

                Regards,
                TradeSphere Team
                """.formatted(
                safeName(fullName),
                code,
                validMinutes
        );

        return sendEmail(to, subject, body);
    }

    @Override
    public boolean sendTemporaryPassword(
            String to,
            String fullName,
            String userName,
            String temporaryPassword) {

        String subject = "Your TradeSphere Account Credentials";

        String body = """
                Hello %s,

                Your TradeSphere account has been created successfully.

                Username: %s
                Temporary Password: %s

                Please sign in using the above credentials.

                For security reasons, you must change your temporary
                password after your first login.

                Please do not share your password with anyone.

                Regards,
                TradeSphere Team
                """.formatted(
                safeName(fullName),
                userName,
                temporaryPassword
        );

        return sendEmail(to, subject, body);
    }

    private boolean sendEmail(
            String to,
            String subject,
            String body) {

        if (to == null || to.isBlank()) {
            log.error(
                    "Email '{}' could not be sent because recipient is blank",
                    subject
            );
            return false;
        }

        if (fromEmail == null || fromEmail.isBlank()) {
            log.error(
                    "Email '{}' could not be sent because app.mail.from is blank",
                    subject
            );
            return false;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();

            message.setFrom(fromEmail.trim());
            message.setTo(to.trim());
            message.setSubject(subject);
            message.setText(body);

            log.info(
                    "Sending email '{}' from '{}' to '{}'",
                    subject,
                    fromEmail,
                    to
            );

            mailSender.send(message);

            log.info(
                    "Email '{}' sent successfully to '{}'",
                    subject,
                    to
            );

            return true;

        } catch (Exception e) {
            log.error(
                    "Email send failed. Subject='{}', recipient='{}', "
                            + "exceptionType='{}', message='{}'",
                    subject,
                    to,
                    e.getClass().getName(),
                    e.getMessage(),
                    e
            );

            return false;
        }
    }

    private String safeName(String fullName) {
        return fullName == null || fullName.isBlank()
                ? "User"
                : fullName.trim();
    }
}