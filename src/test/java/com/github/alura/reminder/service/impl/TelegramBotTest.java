package com.github.alura.reminder.service.impl;


import com.github.alura.reminder.event.TelegramAccountLinkedEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@ExtendWith(MockitoExtension.class)
class TelegramBotTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private TelegramBot telegramBot;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(telegramBot, "botUsername", "test_bot");
    }

    @Test
    void onUpdateReceived_shouldPublishEvent_whenStartCommandWithToken() {
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);

        Mockito.when(update.hasMessage()).thenReturn(true);
        Mockito.when(update.getMessage()).thenReturn(message);
        Mockito.when(message.hasText()).thenReturn(true);
        Mockito.when(message.getText()).thenReturn("/start abc123");
        Mockito.when(message.getChatId()).thenReturn(42L);

        telegramBot.onUpdateReceived(update);

        ArgumentCaptor<TelegramAccountLinkedEvent> captor = ArgumentCaptor.forClass(TelegramAccountLinkedEvent.class);
        Mockito.verify(eventPublisher).publishEvent(captor.capture());

        TelegramAccountLinkedEvent event = captor.getValue();
        Assertions.assertEquals("abc123", event.getToken());
        Assertions.assertEquals("42", event.getChatId());
    }

    @Test
    void sendMessage_shouldCallExecute() throws TelegramApiException {
        TelegramBot spyBot = Mockito.spy(telegramBot);
        Mockito.doReturn(new Message()).when(spyBot).execute(Mockito.any(SendMessage.class));

        spyBot.sendMessage("42", "Hello");

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        Mockito.verify(spyBot).execute(captor.capture());

        SendMessage sentMessage = captor.getValue();
        Assertions.assertEquals("42", sentMessage.getChatId());
        Assertions.assertEquals("Hello", sentMessage.getText());
    }
}
