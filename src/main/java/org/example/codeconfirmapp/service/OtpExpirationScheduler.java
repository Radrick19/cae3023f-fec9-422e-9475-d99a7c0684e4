package org.example.codeconfirmapp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OtpExpirationScheduler {
    private static final Logger log = LoggerFactory.getLogger(OtpExpirationScheduler.class);

    private final OtpService otpService;

    public OtpExpirationScheduler(OtpService otpService) {
        this.otpService = otpService;
    }

    @Scheduled(fixedDelayString = "${app.otp.expiration-job-interval-ms:60000}")
    public void expireCodes() {
        try {
            var expired = otpService.expireOutdated();
            if (expired > 0) {
                log.info("Переведено в EXPIRED {} OTP-кодов", expired);
            }
        } catch (Exception e) {
            log.error("Не удалось обновить просроченные OTP-коды", e);
        }
    }
}
