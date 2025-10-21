package com.github.alura.reminder.service.impl;

import com.github.alura.reminder.config.TestcontainersConfiguration;
import com.github.alura.reminder.entity.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class TelegramAccountLinkServiceIT {

    public static final String TEST_CHAT_ID = "12345";
    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private TelegramAccountLinkService telegramAccountLinkService;

    @MockitoBean
    private MailSender mailSender;

    @MockitoSpyBean
    private TelegramBot telegramBot;

    @Test
    void fullCycle_shouldLinkAccountViaBotOnUpdateReceived() throws TelegramApiException {
        Mockito.doAnswer(invocation -> null)
                .when(telegramBot).sendMessage(Mockito.anyString(), Mockito.anyString());

        String email = "user@example.com";
        String login = "user1";

        User user = userService.getOrCreateUserFromGoogle(login, email);

        ArgumentCaptor<SimpleMailMessage> mailCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        Mockito.verify(mailSender).send(mailCaptor.capture());
        String sentEmailText = mailCaptor.getValue().getText();

        String token = sentEmailText.split("https://t.me/reminder_bot\\?start=")[1].trim();

        Update update = new Update();
        Message message = new Message();
        Chat chat = new Chat();
        chat.setId(12345L);
        message.setChat(chat);
        message.setText("/start " + token);
        update.setMessage(message);

        telegramBot.onUpdateReceived(update);

        User updatedUser = userService.getOrCreateUserFromGoogle(login, email);
        Assertions.assertEquals(user.getId(), updatedUser.getId());
        Assertions.assertEquals(TEST_CHAT_ID, updatedUser.getTelegramChatId());

        ArgumentCaptor<String> chatIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> messageIdCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(telegramBot).sendMessage(chatIdCaptor.capture(), messageIdCaptor.capture());
        Assertions.assertEquals(TEST_CHAT_ID, chatIdCaptor.getValue());
        Assertions.assertEquals("Ваш аккаунт Telegram успешно привязан.", messageIdCaptor.getValue());
    }
}