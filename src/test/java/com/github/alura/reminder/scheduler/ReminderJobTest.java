package com.github.alura.reminder.scheduler;

import com.github.alura.reminder.entity.Reminder;
import com.github.alura.reminder.service.NotificationService;
import com.github.alura.reminder.service.ReminderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.quartz.JobExecutionContext;

import java.util.List;

class ReminderJobTest {
    private ReminderService reminderService;
    private NotificationService notificationService;
    private ReminderJob reminderJob;

    @BeforeEach
    void setUp() {
        reminderService = Mockito.mock(ReminderService.class);
        notificationService = Mockito.mock(NotificationService.class);
        reminderJob = new ReminderJob(reminderService, notificationService);
    }

    @Test
    void execute_shouldSendNotificationsAndMarkAsSent(){
        Reminder reminder1 = new Reminder();
        reminder1.setTitle("reminder 1");
        Reminder reminder2 = new Reminder();
        reminder2.setTitle("reminder2");
        List<Reminder> dueReminders = List.of(reminder1, reminder2);

        Mockito.when(reminderService.getDueReminders()).thenReturn(dueReminders);

        JobExecutionContext context = Mockito.mock(JobExecutionContext.class);

        reminderJob.execute(context);

        Mockito.verify(reminderService, Mockito.times(1)).getDueReminders();
        Mockito.verify(notificationService).send(reminder1);
        Mockito.verify(notificationService).send(reminder2);
        Mockito.verify(reminderService).markAsSent(reminder1);
        Mockito.verify(reminderService).markAsSent(reminder2);
    }

    @Test
    void execute_shouldDoNothingIfNoDueReminders(){
        Mockito.when(reminderService.getDueReminders()).thenReturn(List.of());
        JobExecutionContext context = Mockito.mock(JobExecutionContext.class);

        reminderJob.execute(context);

        Mockito.verify(notificationService, Mockito.never()).send(Mockito.any());
        Mockito.verify(reminderService, Mockito.never()).markAsSent(Mockito.any());
    }
}
