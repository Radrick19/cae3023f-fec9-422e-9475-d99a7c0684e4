package org.example.codeconfirmapp.service;

import org.example.codeconfirmapp.model.DeliveryChannel;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationService {
    private final Map<DeliveryChannel, NotificationSender> senders;

    public NotificationService(List<NotificationSender> senders) {
        this.senders = new EnumMap<>(DeliveryChannel.class);
        for (NotificationSender sender : senders) {
            this.senders.put(sender.supports(), sender);
        }
    }

    public void send(DeliveryChannel channel, String destination, String code) {
        NotificationSender sender = senders.get(channel);
        if (sender == null) {
            throw new IllegalArgumentException("Неподдерживаемый канал доставки: " + channel);
        }
        sender.send(destination, code);
    }
}
