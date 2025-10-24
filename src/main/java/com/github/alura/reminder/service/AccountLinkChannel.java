package com.github.alura.reminder.service;

import com.github.alura.reminder.entity.User;

public interface AccountLinkChannel {
    void sendLink(User user, String invitationLink);
}
