package com.github.alura.reminder.service;

import com.github.alura.reminder.dto.ReminderDto;
import com.github.alura.reminder.dto.ReminderListRequest;
import com.github.alura.reminder.dto.ReminderRequestDto;
import com.github.alura.reminder.entity.Reminder;
import com.github.alura.reminder.entity.User;
import com.github.alura.reminder.exception.NotFoundException;
import com.github.alura.reminder.filter.ReminderFilter;
import com.github.alura.reminder.mapper.ReminderMapper;
import com.github.alura.reminder.repository.ReminderRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.mockito.Mockito;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class ReminderServiceTest {
    @Mock
    private ReminderRepository reminderRepository;

    @Mock
    private ReminderMapper reminderMapper;

    @Mock
    private UserService userService;

    @InjectMocks
    private ReminderService reminderService;

    private User user;
    private Reminder existingReminder;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(1L);

        existingReminder = new Reminder();
        existingReminder.setId(1L);
        existingReminder.setTitle("Old Title");
        existingReminder.setDescription("Old Desc");
        existingReminder.setRemind(LocalDateTime.now());
        existingReminder.setUser(user);
    }

    @Test
    void createReminder_shouldSaveReminderForCurrentUser() {
        ReminderRequestDto dto = new ReminderRequestDto("Title", "Desc", LocalDateTime.now());
        Reminder reminderEntity = new Reminder();
        reminderEntity.setTitle("Title");

        Reminder savedEntity = new Reminder();
        savedEntity.setId(10L);
        savedEntity.setTitle("Title");
        savedEntity.setUser(user);

        ReminderDto savedDto = new ReminderDto(10L, "Title", "Desc", reminderEntity.getRemind(), user.getId(), false);

        Mockito.when(reminderMapper.toEntity(dto)).thenReturn(reminderEntity);
        Mockito.when(userService.getCurrentUser()).thenReturn(user);
        Mockito.when(reminderRepository.save(reminderEntity)).thenReturn(savedEntity);
        Mockito.when(reminderMapper.toDto(savedEntity)).thenReturn(savedDto);

        ReminderDto result = reminderService.createReminder(dto);

        Assertions.assertEquals(10L, result.getId());
        Assertions.assertEquals("Title", result.getTitle());

        Mockito.verify(reminderRepository).save(reminderEntity);
        Mockito.verify(reminderMapper).toEntity(dto);
        Mockito.verify(reminderMapper).toDto(savedEntity);
    }

    @Test
    void deleteReminder_shouldDeleteIfOwner() {
        Reminder reminder = new Reminder();
        reminder.setId(1L);
        reminder.setUser(user);

        Mockito.when(userService.getCurrentUser()).thenReturn(user);
        Mockito.when(reminderRepository.findById(1L)).thenReturn(Optional.of(reminder));

        reminderService.deleteReminder(1L);

        Mockito.verify(reminderRepository).delete(reminder);
    }

    @Test
    void deleteReminder_shouldThrowIfNotOwner() {
        User anotherUser = new User();
        anotherUser.setId(2L);

        Reminder reminder = new Reminder();
        reminder.setId(1L);
        reminder.setUser(anotherUser);

        Mockito.when(userService.getCurrentUser()).thenReturn(user);
        Mockito.when(reminderRepository.findById(1L)).thenReturn(Optional.of(reminder));

        Assertions.assertThrows(AccessDeniedException.class,
                        () -> reminderService.deleteReminder(1L));

        Mockito.verify(reminderRepository, Mockito.never()).delete(Mockito.any(Reminder.class));
    }

    @Test
    void deleteReminder_shouldThrowIfNotFound() {
        Mockito.when(reminderRepository.findById(999L)).thenReturn(Optional.empty());
        Mockito.when(userService.getCurrentUser()).thenReturn(user);

        Assertions.assertThrows(NotFoundException.class, () -> reminderService.deleteReminder(999L));
    }

    @Test
    void getReminders_shouldReturnPagedDtos() {
        ReminderListRequest request = new ReminderListRequest();
        ReminderFilter filter = Mockito.mock(ReminderFilter.class);
        Pageable pageable = PageRequest.of(0, 10);

        Reminder reminder1 = new Reminder();
        reminder1.setId(1L);
        reminder1.setTitle("Test1");

        Reminder reminder2 = new Reminder();
        reminder2.setId(2L);
        reminder2.setTitle("Test2");

        Page<Reminder> page = new PageImpl<>(List.of(reminder1, reminder2), pageable, 2);

        ReminderDto dto1 = new ReminderDto(1L, "Test1", "Desc1", LocalDateTime.now(), user.getId(), false);
        ReminderDto dto2 = new ReminderDto(2L, "Test2", "Desc2", LocalDateTime.now(), user.getId(), false);

        Mockito.when(userService.getCurrentUser()).thenReturn(user);
        Mockito.when(reminderMapper.toFilter(request, user.getId())).thenReturn(filter);
        Mockito.when(reminderMapper.toPageable(request)).thenReturn(pageable);
        Mockito.when(reminderRepository.findAll(filter, pageable)).thenReturn(page);
        Mockito.when(reminderMapper.toDto(reminder1)).thenReturn(dto1);
        Mockito.when(reminderMapper.toDto(reminder2)).thenReturn(dto2);

        Page<ReminderDto> result = reminderService.getReminders(request);

        Assertions.assertEquals(2, result.getContent().size());
        Assertions.assertEquals("Test1", result.getContent().get(0).getTitle());
        Mockito.verify(reminderRepository).findAll(filter, pageable);
    }

    @Test
    void updateReminder_shouldUpdateFieldsSuccessfully() {
        ReminderRequestDto dto = new ReminderRequestDto();
        dto.setTitle("New Title");
        dto.setDescription("New Desc");
        dto.setRemind(LocalDateTime.now().plusDays(1));

        Mockito.when(userService.getCurrentUser()).thenReturn(user);
        Mockito.when(reminderRepository.findById(1L)).thenReturn(Optional.of(existingReminder));

        Mockito.doAnswer(invocation -> {
            ReminderRequestDto source = invocation.getArgument(0);
            Reminder target = invocation.getArgument(1);
            target.setTitle(source.getTitle());
            target.setDescription(source.getDescription());
            target.setRemind(source.getRemind());
            return null;
        }).when(reminderMapper).updateFromDto(Mockito.any(ReminderRequestDto.class), Mockito.any(Reminder.class));

        Mockito.when(reminderRepository.save(existingReminder)).thenReturn(existingReminder);
        Mockito.when(reminderMapper.toDto(Mockito.any(Reminder.class)))
                .thenAnswer(invocation -> {
                    Reminder rem = invocation.getArgument(0);
                    ReminderDto reminderDto = new ReminderDto();
                    reminderDto.setId(rem.getId());
                    reminderDto.setTitle(rem.getTitle());
                    reminderDto.setDescription(rem.getDescription());
                    reminderDto.setRemind(rem.getRemind());
                    return reminderDto;
                });

        ReminderDto updated = reminderService.updateReminder(1L, dto);

        Assertions.assertEquals("New Title", updated.getTitle());
        Assertions.assertEquals("New Desc", updated.getDescription());
        Assertions.assertEquals(existingReminder.getId(), updated.getId());

        Mockito.verify(reminderRepository).save(existingReminder);
        Mockito.verify(reminderMapper).updateFromDto(dto, existingReminder);
    }


    @Test
    void updateReminder_shouldThrowEntityNotFoundException_whenReminderNotFound() {
        ReminderRequestDto dto = new ReminderRequestDto();
        dto.setDescription("new description");

        Mockito.when(userService.getCurrentUser()).thenReturn(user);
        Mockito.when(reminderRepository.findById(42L)).thenReturn(Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(NotFoundException.class, () -> reminderService.updateReminder(42L, dto));

        Mockito.verify(reminderRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void getDueReminders_shouldReturnOnlyNotSentRemindersBeforeNow() {
        Reminder reminder1 = new Reminder();
        reminder1.setId(1L);
        reminder1.setTitle("Reminder 1");
        reminder1.setRemind(LocalDateTime.now().minusHours(1));
        reminder1.setSent(false);

        Reminder reminder2 = new Reminder();
        reminder2.setId(2L);
        reminder2.setTitle("Reminder 2");
        reminder2.setRemind(LocalDateTime.now().minusMinutes(30));
        reminder2.setSent(false);

        List<Reminder> dueReminders = List.of(reminder1, reminder2);

        Mockito.when(reminderRepository.findAllByRemindBeforeAndIsSentFalse(Mockito.any()))
                .thenReturn(dueReminders);

        List<Reminder> result = reminderService.getDueReminders();

        Assertions.assertEquals(2, result.size());
        Assertions.assertTrue(result.contains(reminder1));
        Assertions.assertTrue(result.contains(reminder2));
        Mockito.verify(reminderRepository).findAllByRemindBeforeAndIsSentFalse(Mockito.any());
    }

    @Test
    void markAsSent_shouldSetSentTrueAndSaveReminder() {
        Reminder reminder = new Reminder();
        reminder.setId(1L);
        reminder.setTitle("Reminder 1");
        reminder.setRemind(LocalDateTime.now().minusHours(1));
        reminder.setSent(false);

        reminderService.markAsSent(reminder);

        Assertions.assertTrue(reminder.isSent());
        Mockito.verify(reminderRepository).save(reminder);
    }
}
