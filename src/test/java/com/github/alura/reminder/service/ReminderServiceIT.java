package com.github.alura.reminder.service;

import com.github.alura.reminder.config.TestcontainersConfiguration;
import com.github.alura.reminder.dto.ReminderDto;
import com.github.alura.reminder.dto.ReminderListRequest;
import com.github.alura.reminder.dto.ReminderRequestDto;
import com.github.alura.reminder.filter.SortField;
import com.github.alura.reminder.entity.Reminder;
import com.github.alura.reminder.entity.User;
import com.github.alura.reminder.mapper.ReminderMapper;
import com.github.alura.reminder.repository.ReminderRepository;
import com.github.alura.reminder.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class ReminderServiceIT {
    @Autowired
    private ReminderRepository reminderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReminderMapper reminderMapper;

    private ReminderService reminderService;
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setup() {
        reminderRepository.deleteAll();
        userRepository.deleteAll();

        testUser = createUser("testUser");

        userService = new UserService() {
            @Override
            public User getCurrentUser() {
                return testUser;
            }

            @Override
            public User getOrCreateUserFromGoogle(String login, String email) {
                return testUser;
            }

            @Override
            public void addChatId(Long userId, String chatId) {

            }
        };

        reminderService = new ReminderService(reminderRepository, reminderMapper, userService);
    }

   @Test
    void createReminder_shouldSaveReminder() {
        ReminderRequestDto dto = new ReminderRequestDto("Title", "Desc", LocalDateTime.now());

        ReminderDto saved = reminderService.createReminder(dto);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTitle()).isEqualTo("Title");

        assertThat(reminderRepository.findAll()).hasSize(1);
        assertThat(reminderRepository.findAll().get(0).getUser().getId()).isEqualTo(testUser.getId());
    }

    @Test
    void deleteReminder_shouldRemoveReminderIfOwner() {
        Reminder reminder = new Reminder();
        reminder.setTitle("ToDelete");
        reminder.setUser(testUser);
        reminder.setRemind(LocalDateTime.now());
        reminder = reminderRepository.save(reminder);

        reminderService.deleteReminder(reminder.getId());

        assertThat(reminderRepository.findById(reminder.getId())).isEmpty();
    }

    @Test
    void deleteReminder_shouldThrowIfNotOwner() {
        User anotherUser = createUser("otherUser");

        Reminder reminder = new Reminder();
        reminder.setTitle("OtherReminder");
        reminder.setUser(anotherUser);
        reminder.setRemind(LocalDateTime.now());
        reminder = reminderRepository.save(reminder);

        Reminder finalReminder = reminder;
        assertThrows(AccessDeniedException.class, () -> reminderService.deleteReminder(finalReminder.getId()));
    }

    @Test
    void getReminders_shouldReturnPagedDtos() {
        for (int i = 0; i < 5; i++) {
            Reminder reminder = new Reminder();
            reminder.setTitle("Reminder " + i);
            reminder.setUser(testUser);
            reminder.setRemind(LocalDateTime.now().plusDays(i));
            reminderRepository.save(reminder);
        }

        ReminderListRequest request = new ReminderListRequest();
        request.setPage(0);
        request.setSize(3);

        Page<ReminderDto> page = reminderService.getReminders(request);

        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getContent()).hasSize(3);
    }

    @Test
    void shouldFindRemindersBySearchInTitle() {
        addReminders();

        ReminderListRequest request = new ReminderListRequest();
        request.setSearch("Meeting");


        Page<ReminderDto> page = reminderService.getReminders(request);

        assertEquals(1, page.getTotalElements());
        assertEquals("Meeting", page.getContent().get(0).getTitle());
    }

    @Test
    void shouldFindRemindersBySearchInDescription() {
        addReminders();

        ReminderListRequest request = new ReminderListRequest();
        request.setSearch("groceries");

        Page<ReminderDto> page = reminderService.getReminders(request);

        assertEquals(1, page.getTotalElements());
        assertEquals("Shopping", page.getContent().get(0).getTitle());
    }

    @Test
    void shouldBeCaseInsensitiveSearch() {
        addReminders();

        ReminderListRequest request = new ReminderListRequest();
        request.setSearch("mEeTinG");

        Page<ReminderDto> page = reminderService.getReminders(request);

        assertEquals(1, page.getTotalElements());
        assertEquals("Meeting", page.getContent().get(0).getTitle());
    }

    @Test
    void shouldReturnAllRemindersWhenSearchIsEmpty() {
        addReminders();

        ReminderListRequest request = new ReminderListRequest();

        Page<ReminderDto> page = reminderService.getReminders(request);

        assertEquals(4, page.getTotalElements());
    }

    @Test
    void shouldReturnEmptyIfSearchNotFound() {
        addReminders();

        ReminderListRequest request = new ReminderListRequest();
        request.setSearch("Birthday");

        Page<ReminderDto> page = reminderService.getReminders(request);

        assertTrue(page.isEmpty());
    }

    @Test
    void shouldSortByDateDescending() {
        addReminders();

        ReminderListRequest request = new ReminderListRequest();
        request.setSortBy(SortField.DATE);
        request.setAsc(false);

        Page<ReminderDto> page = reminderService.getReminders(request);

        List<LocalDateTime> dates = page.getContent().stream()
                .map(ReminderDto::getRemind)
                .collect(Collectors.toList());

        List<LocalDateTime> sorted = new ArrayList<>(dates);
        sorted.sort(Comparator.reverseOrder());

        assertEquals(sorted, dates);
    }

    @Test
    void shouldPaginateResults() {
        addReminders();

        ReminderListRequest request = new ReminderListRequest();
        request.setPage(0);
        request.setSize(2);

        Page<ReminderDto> firstPage = reminderService.getReminders(request);
        assertEquals(2, firstPage.getNumberOfElements());

        request.setPage(1);
        Page<ReminderDto> secondPage = reminderService.getReminders(request);
        assertEquals(2, secondPage.getNumberOfElements());

        assertNotEquals(firstPage.getContent().get(0).getId(), secondPage.getContent().get(0).getId());
    }

    @Test
    void shouldFilterByFromDateOnly() {
        addReminders();
        ReminderListRequest request = new ReminderListRequest();
        request.setFrom(LocalDateTime.of(2025, 10, 21, 0, 0));

        Page<ReminderDto> page = reminderService.getReminders(request);

        List<String> titles = page.getContent().stream().map(ReminderDto::getTitle).toList();
        assertEquals(3, page.getTotalElements());
        assertTrue(titles.containsAll(List.of("Shopping", "Dentist", "Workout")));
    }

    @Test
    void shouldFilterByDateAndSearchText() {
        addReminders();
        ReminderListRequest request = new ReminderListRequest();
        request.setSearch("meeting");
        request.setFrom(LocalDateTime.of(2025, 10, 20, 0, 0));
        request.setTo(LocalDateTime.of(2025, 10, 21, 23, 59));

        Page<ReminderDto> page = reminderService.getReminders(request);

        assertEquals(1, page.getTotalElements());

        ReminderDto result = page.getContent().get(0);
        assertEquals("Meeting", result.getTitle());
        assertTrue(result.getRemind().isAfter(LocalDateTime.of(2025, 10, 19, 23, 59)));
        assertTrue(result.getRemind().isBefore(LocalDateTime.of(2025, 10, 22, 0, 0)));
    }

    private User createUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setTelegramAccount(username);
        return userRepository.save(user);
    }

    private void addReminders() {
        List<Reminder> reminders = List.of(
                new Reminder(null, "Meeting", "Team meeting with project updates",
                        LocalDateTime.of(2025, 10, 20, 9, 0), testUser, false),

                new Reminder(null, "Shopping", "Buy groceries and cleaning supplies",
                        LocalDateTime.of(2025, 10, 21, 15, 0), testUser, false),

                new Reminder(null, "Dentist", "Dentist appointment at 10 AM",
                        LocalDateTime.of(2025, 10, 22, 10, 0), testUser, false),

                new Reminder(null, "Workout", "Morning run and gym",
                        LocalDateTime.of(2025, 10, 23, 7, 30), testUser, false)
        );

        reminderRepository.saveAll(reminders);
    }
}
