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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReminderService {
    private final ReminderRepository reminderRepository;
    private final ReminderMapper reminderMapper;
    private final UserService userService;

    public ReminderDto createReminder(ReminderRequestDto reminderDto) {
        Reminder reminder = reminderMapper.toEntity(reminderDto);
        User user = userService.getCurrentUser();

        reminder.setUser(user);
        reminder = reminderRepository.save(reminder);
        return reminderMapper.toDto(reminder);
    }

    public void deleteReminder(Long id) {
        User user = userService.getCurrentUser();
        Reminder reminder = getReminderById(id);

        checkOwner(reminder, user);

        reminderRepository.delete(reminder);
    }

    public Page<ReminderDto> getReminders(ReminderListRequest request) {
        User user = userService.getCurrentUser();

        ReminderFilter filter = reminderMapper.toFilter(request, user.getId());
        Pageable pageable = reminderMapper.toPageable(request);

        Page<Reminder> page = reminderRepository.findAll(filter, pageable);
        return page.map(reminderMapper::toDto);
    }

    public ReminderDto updateReminder(Long reminderId, ReminderRequestDto dto) {
        User user = userService.getCurrentUser();

        Reminder reminder = getReminderById(reminderId);

        checkOwner(reminder, user);

        reminderMapper.updateFromDto(dto, reminder);

        reminder = reminderRepository.save(reminder);

        return reminderMapper.toDto(reminder);
    }

    private Reminder getReminderById(Long reminderId) {
        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new EntityNotFoundException("Reminder not found"));
        return reminder;
    }

    private static void checkOwner(Reminder reminder, User user) {
        if (!reminder.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not allowed to edit this reminder");
        }
    }
}
