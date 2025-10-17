package com.github.alura.reminder.service;

import com.github.alura.reminder.dto.ReminderDto;
import com.github.alura.reminder.dto.ReminderListRequest;
import com.github.alura.reminder.dto.ReminderRequestDto;
import com.github.alura.reminder.entity.Reminder;
import com.github.alura.reminder.entity.User;
import com.github.alura.reminder.filter.ReminderFilter;
import com.github.alura.reminder.mapper.ReminderMapper;
import com.github.alura.reminder.repository.ReminderRepository;
import jakarta.persistence.EntityNotFoundException;
import org.assertj.core.api.Assertions;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

        ReminderDto savedDto = new ReminderDto(10L, "Title", "Desc", reminderEntity.getRemind(), user.getId());

        Mockito.when(reminderMapper.toEntity(dto)).thenReturn(reminderEntity);
        Mockito.when(userService.getCurrentUser()).thenReturn(user);
        Mockito.when(reminderRepository.save(reminderEntity)).thenReturn(savedEntity);
        Mockito.when(reminderMapper.toDto(savedEntity)).thenReturn(savedDto);

        // when
        ReminderDto result = reminderService.createReminder(dto);

        // then
        Assertions.assertThat(result.getId()).isEqualTo(10L);
        Assertions.assertThat(result.getTitle()).isEqualTo("Title");

        Mockito.verify(reminderRepository).save(reminderEntity);
        Mockito.verify(reminderMapper).toEntity(dto);
        Mockito.verify(reminderMapper).toDto(savedEntity);
    }

    @Test
    void deleteReminder_shouldDeleteIfOwner() {
        // given
        Reminder reminder = new Reminder();
        reminder.setId(1L);
        reminder.setUser(user);

        Mockito.when(userService.getCurrentUser()).thenReturn(user);
        Mockito.when(reminderRepository.findById(1L)).thenReturn(Optional.of(reminder));

        // when
        reminderService.deleteReminder(1L);

        // then
        Mockito.verify(reminderRepository).delete(reminder);
    }

    @Test
    void deleteReminder_shouldThrowIfNotOwner() {
        // given
        User anotherUser = new User();
        anotherUser.setId(2L);

        Reminder reminder = new Reminder();
        reminder.setId(1L);
        reminder.setUser(anotherUser);

        Mockito.when(userService.getCurrentUser()).thenReturn(user);
        Mockito.when(reminderRepository.findById(1L)).thenReturn(Optional.of(reminder));

        // when / then
        Assertions.assertThatThrownBy(() -> reminderService.deleteReminder(1L))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("not allowed");

        Mockito.verify(reminderRepository, Mockito.never()).delete(Mockito.any(Reminder.class));
    }

    @Test
    void deleteReminder_shouldThrowIfNotFound() {
        // given
        Mockito.when(reminderRepository.findById(999L)).thenReturn(Optional.empty());
        Mockito.when(userService.getCurrentUser()).thenReturn(user);

        // when / then
        Assertions.assertThatThrownBy(() -> reminderService.deleteReminder(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Reminder not found");
    }

    @Test
    void getReminders_shouldReturnPagedDtos() {
        // given
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

        ReminderDto dto1 = new ReminderDto(1L, "Test1", "Desc1", LocalDateTime.now(), user.getId());
        ReminderDto dto2 = new ReminderDto(2L, "Test2", "Desc2", LocalDateTime.now(), user.getId());

        Mockito.when(userService.getCurrentUser()).thenReturn(user);
        Mockito.when(reminderMapper.toFilter(request, user.getId())).thenReturn(filter);
        Mockito.when(reminderMapper.toPageable(request)).thenReturn(pageable);
        Mockito.when(reminderRepository.findAll(filter, pageable)).thenReturn(page);
        Mockito.when(reminderMapper.toDto(reminder1)).thenReturn(dto1);
        Mockito.when(reminderMapper.toDto(reminder2)).thenReturn(dto2);

        // when
        Page<ReminderDto> result = reminderService.getReminders(request);

        // then
        Assertions.assertThat(result.getContent()).hasSize(2);
        Assertions.assertThat(result.getContent().get(0).getTitle()).isEqualTo("Test1");
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

        // MapStruct просто обновляет поля
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

        assertEquals("New Title", updated.getTitle());
        assertEquals("New Desc", updated.getDescription());
        assertEquals(existingReminder.getId(), updated.getId());

        Mockito.verify(reminderRepository).save(existingReminder);
        Mockito.verify(reminderMapper).updateFromDto(dto, existingReminder);
    }


    @Test
    void updateReminder_shouldThrowEntityNotFoundException_whenReminderNotFound() {
        ReminderRequestDto dto = new ReminderRequestDto();
        dto.setDescription("new description");

        Mockito.when(userService.getCurrentUser()).thenReturn(user);
        Mockito.when(reminderRepository.findById(42L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> reminderService.updateReminder(42L, dto));

        Mockito.verify(reminderRepository, Mockito.never()).save(Mockito.any());
    }
}
