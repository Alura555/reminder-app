package com.github.alura.reminder.service.impl;

import com.github.alura.reminder.entity.Reminder;
import com.github.alura.reminder.entity.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

import java.util.Objects;

@ExtendWith(MockitoExtension.class)
class EmailMessageServiceTest {

    @Mock
    private MailSender mailSender;

    @InjectMocks
    private EmailMessageService emailService;

    @Test
    void send_shouldCallMailSenderWithCorrectMessage() {
        User user = new User();
        user.setEmail("test@example.com");

        Reminder reminder = new Reminder();
        reminder.setUser(user);
        reminder.setTitle("Test Reminder");
        reminder.setDescription("Test description");

        emailService.send(reminder);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        Mockito.verify(mailSender).send(captor.capture());

        SimpleMailMessage sentMessage = captor.getValue();
        Assertions.assertEquals("test@example.com", Objects.requireNonNull(sentMessage.getTo())[0]);
        Assertions.assertEquals("Test Reminder", sentMessage.getSubject());
        Assertions.assertEquals("Test description", sentMessage.getText());
    }

    @Test
    void sendLink_shouldCallMailSender() {
        User user = User.builder().email("test@example.com").username("John").build();
        String link = "https://t.me/bot?start=token123";

        emailService.sendLink(user, link);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        Mockito.verify(mailSender, Mockito.times(1)).send(captor.capture());

        SimpleMailMessage message = captor.getValue();
        Assertions.assertEquals("test@example.com", message.getTo()[0]);
        Assertions.assertEquals("Приглашение в систему напоминаний", message.getSubject());
        Assertions.assertTrue(message.getText().contains(link));
        Assertions.assertTrue(message.getText().contains("John"));
    }
}
