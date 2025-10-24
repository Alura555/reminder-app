package com.github.alura.reminder.service;

import com.github.alura.reminder.entity.Reminder;

public interface NotificationService {
    void send(Reminder reminder);
}
