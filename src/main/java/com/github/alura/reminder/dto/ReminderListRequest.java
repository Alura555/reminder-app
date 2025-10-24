package com.github.alura.reminder.dto;

import com.github.alura.reminder.filter.SortField;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReminderListRequest {
    private int page = 0;
    private int size = 10;
    private SortField sortBy = SortField.DATE;
    private String search;
    private boolean asc = true;
    private LocalDateTime from;
    private LocalDateTime to;
}
