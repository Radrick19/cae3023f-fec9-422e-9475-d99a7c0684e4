package org.example.codeconfirmapp.service;

import org.example.codeconfirmapp.model.DeliveryChannel;
import org.jsmpp.bean.Alphabet;
import org.jsmpp.bean.BindType;
import org.jsmpp.bean.ESMClass;
import org.jsmpp.bean.GeneralDataCoding;
import org.jsmpp.bean.NumberingPlanIndicator;
import org.jsmpp.bean.RegisteredDelivery;
import org.jsmpp.bean.SMSCDeliveryReceipt;
import org.jsmpp.bean.TypeOfNumber;
import org.jsmpp.session.BindParameter;
import org.jsmpp.session.SMPPSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class SmsNotificationSender implements NotificationSender {
    private final String host;
    private final int port;
    private final String systemId;
    private final String password;
    private final String systemType;
    private final String sourceAddress;

    public SmsNotificationSender(
            @Value("${smpp.host:localhost}") String host,
            @Value("${smpp.port:2775}") int port,
            @Value("${smpp.system_id:smppclient1}") String systemId,
            @Value("${smpp.password:password}") String password,
            @Value("${smpp.system_type:OTP}") String systemType,
            @Value("${smpp.source_addr:OTPService}") String sourceAddress
    ) {
        this.host = host;
        this.port = port;
        this.systemId = systemId;
        this.password = password;
        this.systemType = systemType;
        this.sourceAddress = sourceAddress;
    }

    @Override
    public DeliveryChannel supports() {
        return DeliveryChannel.SMS;
    }

    @Override
    public void send(String destination, String code) {
        var session = new SMPPSession();
        try {
            var bindParameter = new BindParameter(
                    BindType.BIND_TX,
                    systemId,
                    password,
                    systemType,
                    TypeOfNumber.UNKNOWN,
                    NumberingPlanIndicator.UNKNOWN,
                    sourceAddress
            );
            session.connectAndBind(host, port, bindParameter);
            session.submitShortMessage(
                    systemType,
                    TypeOfNumber.UNKNOWN,
                    NumberingPlanIndicator.UNKNOWN,
                    sourceAddress,
                    TypeOfNumber.UNKNOWN,
                    NumberingPlanIndicator.UNKNOWN,
                    destination,
                    new ESMClass(),
                    (byte) 0,
                    (byte) 1,
                    null,
                    null,
                    new RegisteredDelivery(SMSCDeliveryReceipt.DEFAULT),
                    (byte) 0,
                    new GeneralDataCoding(Alphabet.ALPHA_DEFAULT),
                    (byte) 0,
                    ("Ваш код подтверждения: " + code).getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            throw new IllegalStateException("Не удалось отправить OTP по SMS", e);
        } finally {
            try {
                session.unbindAndClose();
            } catch (Exception ignored) {
                // Игнорируем ошибки при закрытии, чтобы они не скрыли исходную ошибку отправки.
            }
        }
    }
}
