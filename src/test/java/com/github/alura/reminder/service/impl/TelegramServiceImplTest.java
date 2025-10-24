package com.github.alura.reminder.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class TelegramServiceImplTest {

    @Mock
    private TelegramBot telegramBot;

    @InjectMocks
    private TelegramServiceImpl telegramService;

    @Test
    void generateInvitationLink_shouldReturnCorrectUrl() {
        Mockito.when(telegramBot.getBotUsername()).thenReturn("TestBot");

        String token = "abc123";

        String link = telegramService.generateInvitationLink(token);

        String expected = "https://t.me/TestBot?start=abc123";
        Assertions.assertEquals(expected, link);
    }

    @Test
    void sendMessage_shouldCallTelegramBotSendMessage() {
        String chatId = "987654";
        String message = "Hello, Telegram!";

        telegramService.sendMessage(chatId, message);

        Mockito.verify(telegramBot).sendMessage(chatId, message);
        Mockito.verifyNoMoreInteractions(telegramBot);
    }
}
