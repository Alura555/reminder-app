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
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Objects;

@ExtendWith(MockitoExtension.class)
class EmailNotificationServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailNotificationService service;

    @Test
    void send_shouldCallMailSenderWithCorrectMessage() {
        User user = new User();
        user.setEmail("test@example.com");

        Reminder reminder = new Reminder();
        reminder.setUser(user);
        reminder.setTitle("Test Reminder");
        reminder.setDescription("Test description");

        service.send(reminder);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        Mockito.verify(mailSender).send(captor.capture());

        SimpleMailMessage sentMessage = captor.getValue();
        Assertions.assertEquals("test@example.com", Objects.requireNonNull(sentMessage.getTo())[0]);
        Assertions.assertEquals("Test Reminder", sentMessage.getSubject());
        Assertions.assertEquals("Test description", sentMessage.getText());
    }
}
