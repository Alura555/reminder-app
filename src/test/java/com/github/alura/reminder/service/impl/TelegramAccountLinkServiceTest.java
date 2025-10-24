package com.github.alura.reminder.service.impl;

import com.github.alura.reminder.entity.User;
import com.github.alura.reminder.event.TelegramAccountLinkedEvent;
import com.github.alura.reminder.event.UserCreatedEvent;
import com.github.alura.reminder.service.AccountLinkChannel;
import com.github.alura.reminder.service.TelegramService;
import com.github.alura.reminder.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TelegramAccountLinkServiceTest {
    @Mock
    private AccountLinkChannel accountLinkChannel;

    @Mock
    private TelegramService telegramService;

    @Mock
    private UserService userService;

    @InjectMocks
    private TelegramAccountLinkService linkService;

    @Test
    void onUserRegistered_shouldSendLink() {
        Mockito.when(telegramService.generateInvitationLink(Mockito.anyString()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");

        UserCreatedEvent event = new UserCreatedEvent(user);
        linkService.onUserRegistered(event);

        ArgumentCaptor<String> linkCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(accountLinkChannel).sendLink(Mockito.eq(user), linkCaptor.capture());

        Assertions.assertNotNull(linkCaptor.getValue());
        Assertions.assertFalse(linkCaptor.getValue().isBlank());
    }

    @Test
    void onTelegramUpdate_shouldSendSuccessMessage() {
        User user = new User();
        user.setId(1L);
        user.setUsername("user");
        user.setEmail("email@example.com");

        Mockito.when(telegramService.generateInvitationLink(Mockito.anyString()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        linkService.sendLink(user);

        ArgumentCaptor<String> linkCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(accountLinkChannel).sendLink(Mockito.eq(user), linkCaptor.capture());
        String capturedToken = linkCaptor.getValue();

        Assertions.assertNotNull(capturedToken);
        Assertions.assertFalse(capturedToken.isBlank());

        String chatId = "42";
        TelegramAccountLinkedEvent event = new TelegramAccountLinkedEvent(capturedToken, chatId);

        linkService.onTelegramUpdate(event);

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(telegramService).sendMessage(Mockito.eq(chatId), messageCaptor.capture());
        Assertions.assertEquals("Ваш аккаунт Telegram успешно привязан.", messageCaptor.getValue());

        Mockito.verify(userService).addChatId(Mockito.eq(user.getId()), Mockito.eq(chatId));
    }

    @Test
    void onTelegramUpdate_shouldSendErrorMessageForInvalidToken() {
        String chatId = "42";
        String token = "invalidToken";

        TelegramAccountLinkedEvent event = new TelegramAccountLinkedEvent(token, chatId);

        linkService.onTelegramUpdate(event);

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(telegramService).sendMessage(Mockito.eq(chatId), messageCaptor.capture());

        Assertions.assertEquals(
                "Не удалось привязать ваш аккаунт Telegram. Попробуйте снова.",
                messageCaptor.getValue()
        );
    }
}
