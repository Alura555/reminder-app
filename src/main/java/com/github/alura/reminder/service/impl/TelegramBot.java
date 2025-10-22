package com.github.alura.reminder.service.impl;

import com.github.alura.reminder.event.TelegramAccountLinkedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {
    private final String botUsername;

    private final ApplicationEventPublisher eventPublisher;

    public TelegramBot(@Value("${telegram.bot.token}") String token,
                       @Value("${telegram.bot.username}") String username,
                       ApplicationEventPublisher eventPublisher) {
        super(token);
        this.botUsername = username;
        this.eventPublisher = eventPublisher;
    }

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
        sendMessage.enableMarkdown(true);
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

    @EventListener(ApplicationReadyEvent.class)
    public void registerBot() {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(this);
            log.debug("Telegram bot registered: " + this.getBotUsername());
        } catch (TelegramApiException e) {
            log.error("Failed to register bot", e);
        }
    }
}
