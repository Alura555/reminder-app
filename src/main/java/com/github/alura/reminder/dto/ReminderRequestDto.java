package com.github.alura.reminder.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReminderRequestDto {
    private String title;
    private String description;
    private LocalDateTime remind;
}