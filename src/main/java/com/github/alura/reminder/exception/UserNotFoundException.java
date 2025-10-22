package com.github.alura.reminder.exception;

public class UserNotFoundException extends NotFoundException {
    public UserNotFoundException(String email) {
        super(String.format("User with email '%s' not found", email));
    }

    public UserNotFoundException(Long userId) {
        super(String.format("User with id '%s' not found", userId));
    }
}
