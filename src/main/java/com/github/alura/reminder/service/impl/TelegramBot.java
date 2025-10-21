package com.github.alura.reminder.service.impl;

import com.github.alura.reminder.event.TelegramAccountLinkedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@RequiredArgsConstructor
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {
    @Value("${telegram.bot.username}")
    private String botUsername;

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        String text = update.getMessage().getText();

        if (text.startsWith("/start ")) {
            String chatId = update.getMessage().getChatId().toString();
            String token = text.substring(7);
            eventPublisher.publishEvent(new TelegramAccountLinkedEvent(token, chatId));
        }
    }

    public void sendMessage(String chatId, String message) {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(message)
                .build();
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Failed to send Telegram message to chatId={}", chatId, e);
        }
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }
}
