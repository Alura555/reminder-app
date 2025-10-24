package com.github.alura.reminder.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class TelegramAccountLinkedEvent {
    private final String token;
    private final String chatId;
}