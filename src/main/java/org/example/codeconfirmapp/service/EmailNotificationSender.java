package org.example.codeconfirmapp.service;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.example.codeconfirmapp.model.DeliveryChannel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
public class EmailNotificationSender implements NotificationSender {
    private final Session session;
    private final String fromEmail;

    public EmailNotificationSender(
            Environment environment,
            @Value("${email.username:}") String username,
            @Value("${email.password:}") String password,
            @Value("${email.from:}") String fromEmail
    ) {
        var properties = new Properties();
        copyIfPresent(environment, properties, "mail.smtp.host");
        copyIfPresent(environment, properties, "mail.smtp.port");
        copyIfPresent(environment, properties, "mail.smtp.auth");
        copyIfPresent(environment, properties, "mail.smtp.starttls.enable");
        this.fromEmail = (fromEmail == null || fromEmail.isBlank()) ? username : fromEmail;
        this.session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    @Override
    public DeliveryChannel supports() {
        return DeliveryChannel.EMAIL;
    }

    @Override
    public void send(String destination, String code) {
        try {
            var message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(destination));
            message.setSubject("Ваш OTP-код");
            message.setText("Ваш код подтверждения: " + code);
            Transport.send(message);
        } catch (Exception e) {
            throw new IllegalStateException("Не удалось отправить OTP по электронной почте", e);
        }
    }

    private void copyIfPresent(Environment environment, Properties properties, String key) {
        var value = environment.getProperty(key);
        if (value != null) {
            properties.setProperty(key, value);
        }
    }
}
