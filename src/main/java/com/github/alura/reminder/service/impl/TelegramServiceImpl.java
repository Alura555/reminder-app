package com.github.alura.reminder.service.impl;

import com.github.alura.reminder.service.TelegramService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TelegramServiceImpl implements TelegramService {
    public static final String BOT_URL_TEMPLATE = "https://t.me/%s?start=%s";
    private final TelegramBot telegramBot;

    @Override
    public String generateInvitationLink(String token) {
        return String.format(BOT_URL_TEMPLATE, telegramBot.getBotUsername(), token);
    }

    @Override
    public void sendMessage(String telegramChatId, String message) {
        telegramBot.sendMessage(telegramChatId, message);
    }
}
