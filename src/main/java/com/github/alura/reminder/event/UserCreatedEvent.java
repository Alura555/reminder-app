package com.github.alura.reminder.event;

import com.github.alura.reminder.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserCreatedEvent {
    private final User user;
}