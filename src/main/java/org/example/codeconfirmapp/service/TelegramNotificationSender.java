package org.example.codeconfirmapp.service;

import org.example.codeconfirmapp.model.DeliveryChannel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Component
public class TelegramNotificationSender implements NotificationSender {
    private final String telegramApiUrl;
    private final String defaultChatId;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public TelegramNotificationSender(
            @Value("${telegram.bot.token:}") String botToken,
            @Value("${telegram.chat.id:}") String defaultChatId
    ) {
        this.telegramApiUrl = "https://api.telegram.org/bot%s/sendMessage".formatted(botToken);
        this.defaultChatId = defaultChatId;
    }

    @Override
    public DeliveryChannel supports() {
        return DeliveryChannel.TELEGRAM;
    }

    @Override
    public void send(String destination, String code) {
        var chatId = (destination == null || destination.isBlank()) ? defaultChatId : destination;
        var text = urlEncode("Ваш код подтверждения: " + code);
        var url = "%s?chat_id=%s&text=%s".formatted(telegramApiUrl, urlEncode(chatId), text);
        var request = HttpRequest.newBuilder().GET().uri(URI.create(url)).build();
        try {
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IllegalStateException("Telegram API вернул статус " + response.statusCode());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Запрос к Telegram был прерван", e);
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось отправить OTP в Telegram", e);
        }
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
