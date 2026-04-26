package org.example.codeconfirmapp.service;

import org.example.codeconfirmapp.model.DeliveryChannel;

public interface NotificationSender {
    DeliveryChannel supports();

    void send(String destination, String code);
}
