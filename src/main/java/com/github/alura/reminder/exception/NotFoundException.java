package com.github.alura.reminder.exception;

public class NotFoundException extends RuntimeException {
    protected NotFoundException(String message) {
        super(message);
    }
}