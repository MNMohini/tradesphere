package com.bnagritech.tradesphere.auth.service.impl;

import com.bnagritech.tradesphere.auth.service.OTPService;
import com.bnagritech.tradesphere.auth.config.TwilioConfig;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class OTPServiceImpl implements OTPService {

    private final TwilioConfig twilioConfig;

    @Override
    public void sendOTP(String phoneNumber) {

        Verification.creator(
                twilioConfig.getVerifyServiceSid(),
                "+" + phoneNumber,
                "sms"
        ).create();
    }

    @Override
    public boolean verifyOTP(String phoneNumber, String otp) {

        VerificationCheck verificationCheck =
                VerificationCheck.creator(twilioConfig.getVerifyServiceSid())
                        .setTo("+" + phoneNumber)
                        .setCode(otp)
                        .create();

        return "approved".equalsIgnoreCase(verificationCheck.getStatus());
    }
}
