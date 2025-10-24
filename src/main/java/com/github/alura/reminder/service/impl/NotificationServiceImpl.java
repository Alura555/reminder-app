package com.github.alura.reminder.service.impl;

import com.github.alura.reminder.entity.Reminder;
import com.github.alura.reminder.service.NotificationChannel;
import com.github.alura.reminder.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final List<NotificationChannel> channels;

    public void send(Reminder reminder) {
        for (NotificationChannel channel : channels) {
            channel.send(reminder);
        }
    }
}
