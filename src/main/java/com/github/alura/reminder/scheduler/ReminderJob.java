package com.github.alura.reminder.scheduler;

import com.github.alura.reminder.service.NotificationService;
import com.github.alura.reminder.service.ReminderService;
import lombok.RequiredArgsConstructor;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReminderJob implements Job {

    private final ReminderService reminderService;
    private final NotificationService notificationService;

    @Override
    public void execute(JobExecutionContext context) {
        reminderService.getDueReminders().forEach(reminder -> {
            notificationService.send(reminder);
            reminderService.markAsSent(reminder);
        });
    }
}