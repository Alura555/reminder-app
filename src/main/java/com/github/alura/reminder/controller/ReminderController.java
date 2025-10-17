package com.github.alura.reminder.controller;

import com.github.alura.reminder.dto.ReminderDto;
import com.github.alura.reminder.dto.ReminderListRequest;
import com.github.alura.reminder.dto.ReminderRequestDto;
import com.github.alura.reminder.service.ReminderService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller("/api/v1/reminder")
@AllArgsConstructor
public class ReminderController {
    private final ReminderService reminderService;

    @PostMapping("/create")
    public ResponseEntity<ReminderDto> createReminder(ReminderRequestDto reminderDto) {
        return ResponseEntity.ok(reminderService.createReminder(reminderDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReminder(@PathVariable Long id) {
        reminderService.deleteReminder(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReminderDto> updateReminder(@PathVariable Long id,
                                                      @RequestBody ReminderRequestDto dto) {
        ReminderDto updated = reminderService.updateReminder(id, dto);
        return ResponseEntity.ok(updated);
    }

    @GetMapping
    public ResponseEntity<Page<ReminderDto>> getReminderList(ReminderListRequest reminderListRequest) {
        return ResponseEntity.ok(reminderService.getReminders(reminderListRequest));
    }
}
