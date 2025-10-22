package com.github.alura.reminder.exception;

public class ReminderNotFoundException extends NotFoundException{
    public ReminderNotFoundException(Long id) {
        super("Reminder with id " + id + " not found");
    }
}
