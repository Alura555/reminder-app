package com.github.alura.reminder.service.impl;

import com.github.alura.reminder.entity.Reminder;
import com.github.alura.reminder.service.NotificationChannel;
import com.github.alura.reminder.service.TelegramService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramNotificationService implements NotificationChannel {
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final TelegramService telegramService;

    @Override
    public void send(Reminder reminder) {
        String telegramChatId = reminder.getUser().getTelegramChatId();
        if (telegramChatId == null || telegramChatId.isBlank()) {
            log.warn("Skipping Telegram notification — user {} has no chatId", reminder.getUser().getId());
            return;
        }

        String message = buildMessage(reminder);
        telegramService.sendMessage(telegramChatId, message);
    }

    private String buildMessage(Reminder reminder) {
        StringBuilder sb = new StringBuilder();
        sb.append("*").append(reminder.getTitle()).append("*").append("\n");
        if (reminder.getDescription() != null && !reminder.getDescription().isBlank()) {
            sb.append(reminder.getDescription()).append("\n");
        }
        if (reminder.getRemind() != null) {
            sb.append("_Remind at: ")
                    .append(reminder.getRemind().format(DATE_FORMATTER))
                    .append("_");
        }
        return sb.toString();
    }
}