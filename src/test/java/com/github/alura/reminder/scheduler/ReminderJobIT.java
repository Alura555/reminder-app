package com.github.alura.reminder.scheduler;

import com.github.alura.reminder.config.TestcontainersConfiguration;
import com.github.alura.reminder.entity.Reminder;
import com.github.alura.reminder.entity.User;
import com.github.alura.reminder.repository.ReminderRepository;
import com.github.alura.reminder.repository.UserRepository;
import com.github.alura.reminder.service.NotificationChannel;
import com.github.alura.reminder.service.ReminderService;
import com.github.alura.reminder.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class ReminderJobIT {

    @Autowired
    private ReminderService reminderService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReminderRepository reminderRepository;

    @Mock
    private NotificationChannel emailNotificationService;

    @Mock
    private NotificationChannel telegramNotificationService;

    private ReminderJob reminderJob;

    private User testUser;

    @BeforeEach
    void setUp() {
        reminderRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setUsername("testUser");
        testUser.setEmail("test@example.com");
        testUser.setTelegramAccount("12345");

        userRepository.save(testUser);

        reminderJob = new ReminderJob(
                reminderService,
                new NotificationServiceImpl(List.of(emailNotificationService, telegramNotificationService))
        );
    }

    @Test
    void execute_shouldCallAllNotificationChannels() throws Exception {
        Reminder reminder = new Reminder();
        reminder.setTitle("Test Reminder");
        reminder.setDescription("Test description");
        reminder.setRemind(LocalDateTime.now().minusMinutes(1));
        reminder.setUser(testUser);
        reminder.setSent(false);

        reminderRepository.save(reminder);

        reminderJob.execute(null);

        Mockito.verify(emailNotificationService, Mockito.times(1))
                .send(Mockito.argThat(r -> r.getId().equals(reminder.getId())));
        Mockito.verify(telegramNotificationService, Mockito.times(1))
                .send(Mockito.argThat(r -> r.getId().equals(reminder.getId())));

        Reminder updated = reminderRepository.findById(reminder.getId()).orElseThrow();
        Assertions.assertTrue(updated.isSent());
    }
}