package com.github.alura.reminder.service;

import com.github.alura.reminder.entity.Reminder;

public interface NotificationChannel {
    void send(Reminder reminder);
}
