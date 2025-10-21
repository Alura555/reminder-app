package com.github.alura.reminder.service;

public interface TelegramService {
    String generateInvitationLink(String token);

    void sendMessage(String telegramChatId, String message);
}
