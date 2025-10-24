package com.github.alura.reminder.service;

import com.github.alura.reminder.entity.User;

public interface AccountLinkService {
    void sendLink(User user);

    void registerAccount(String token, String chatId) throws IllegalArgumentException;
}
