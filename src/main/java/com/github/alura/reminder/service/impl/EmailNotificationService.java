package com.github.alura.reminder.service.impl;

import com.github.alura.reminder.entity.Reminder;
import com.github.alura.reminder.service.NotificationChannel;
import lombok.AllArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EmailNotificationService implements NotificationChannel {
    private final JavaMailSender mailSender;

    @Override
    public void send(Reminder reminder) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(reminder.getUser().getEmail());
        message.setSubject(reminder.getTitle());
        message.setText(reminder.getDescription());
        mailSender.send(message);
    }
}