package com.github.alura.reminder.service.impl;

import com.github.alura.reminder.entity.Reminder;
import com.github.alura.reminder.entity.User;
import com.github.alura.reminder.service.TelegramService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
class TelegramNotificationServiceTest {

    @Mock
    private TelegramService telegramService;

    @InjectMocks
    private TelegramNotificationService telegramNotificationService;

    private User user;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(1L);
        user.setUsername("testUser");
        user.setTelegramChatId("12345");
    }

    @Test
    void send_shouldSendFormattedMessage_whenChatIdExists() {
        Reminder reminder = new Reminder();
        reminder.setTitle("Test Reminder");
        reminder.setDescription("Do something important");
        reminder.setRemind(LocalDateTime.of(2025, 10, 21, 15, 0));
        reminder.setUser(user);

        telegramNotificationService.send(reminder);

        ArgumentCaptor<String> chatIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(telegramService).sendMessage(chatIdCaptor.capture(), messageCaptor.capture());

        Assertions.assertEquals("12345", chatIdCaptor.getValue());
        Assertions.assertTrue(messageCaptor.getValue().contains("*Test Reminder*"));
        Assertions.assertTrue(messageCaptor.getValue().contains("Do something important"));
        Assertions.assertTrue(messageCaptor.getValue().contains("_Remind at: 2025-10-21 15:00_"));
    }

    @Test
    void send_shouldSkip_whenChatIdIsNull() {
        user.setTelegramChatId(null);
        Reminder reminder = new Reminder();
        reminder.setTitle("Test");
        reminder.setUser(user);

        telegramNotificationService.send(reminder);

        Mockito.verifyNoInteractions(telegramService);
    }

    @Test
    void send_shouldSkip_whenChatIdIsBlank() {
        user.setTelegramChatId("   ");
        Reminder reminder = new Reminder();
        reminder.setTitle("Test");
        reminder.setUser(user);

        telegramNotificationService.send(reminder);

        Mockito.verifyNoInteractions(telegramService);
    }

    @Test
    void send_shouldHandleEmptyDescription() {
        Reminder reminder = new Reminder();
        reminder.setTitle("Only Title");
        reminder.setDescription("");
        reminder.setRemind(LocalDateTime.of(2025, 10, 21, 15, 0));
        reminder.setUser(user);

        telegramNotificationService.send(reminder);

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(telegramService).sendMessage(Mockito.eq("12345"), messageCaptor.capture());

        Assertions.assertTrue(messageCaptor.getValue().startsWith("*Only Title*"));
    }

    @Test
    void send_shouldHandleNullRemindDate() {
        Reminder reminder = new Reminder();
        reminder.setTitle("No Date");
        reminder.setDescription("Some desc");
        reminder.setUser(user);

        telegramNotificationService.send(reminder);

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(telegramService).sendMessage(Mockito.eq("12345"), messageCaptor.capture());

        Assertions.assertFalse(messageCaptor.getValue().contains("Remind at:"));
    }
}
