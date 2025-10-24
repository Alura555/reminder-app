package com.github.alura.reminder.service;

import com.github.alura.reminder.entity.User;

public interface UserService {
    User getCurrentUser();

    User getOrCreateUserFromGoogle(String login, String email);

    void addChatId(Long userId, String chatId);
}
