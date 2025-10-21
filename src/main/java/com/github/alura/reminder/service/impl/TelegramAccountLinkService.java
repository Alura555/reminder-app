package com.github.alura.reminder.service.impl;

import com.github.alura.reminder.entity.User;
import com.github.alura.reminder.event.TelegramAccountLinkedEvent;
import com.github.alura.reminder.event.UserCreatedEvent;
import com.github.alura.reminder.service.AccountLinkChannel;
import com.github.alura.reminder.service.AccountLinkService;
import com.github.alura.reminder.service.TelegramService;
import com.github.alura.reminder.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramAccountLinkService implements AccountLinkService {
    private static final long TOKEN_TTL_SECONDS = 300;

    private final AccountLinkChannel accountLinkChannel;
    private final TelegramService telegramService;
    private final UserService userService;
    private final Map<String, TokenEntry> tokens = new ConcurrentHashMap<>();

    @EventListener
    public void onUserRegistered(UserCreatedEvent event) {
        sendLink(event.getUser());
    }

    @EventListener
    public void onTelegramUpdate(TelegramAccountLinkedEvent event) {
        String message = registerAccountAndGetMessage(event);
        telegramService.sendMessage(event.getChatId(), message);
    }

    private String registerAccountAndGetMessage(TelegramAccountLinkedEvent event) {
        try {
            registerAccount(event.getToken(), event.getChatId());
            log.info("Telegram account linked for user with token {}", event.getToken());
            return "Ваш аккаунт Telegram успешно привязан.";
        } catch (Exception e) {
            log.warn("Failed to link Telegram account for token {}", event.getToken(), e);
            return "Не удалось привязать ваш аккаунт Telegram. Попробуйте снова.";
        }
    }

    @Override
    public void sendLink(User user) {
        String token = generateToken(user.getId());
        String invitationLink = telegramService.generateInvitationLink(token);
        accountLinkChannel.sendLink(user, invitationLink);
    }

    @Override
    public void registerAccount(String token, String chatId) throws IllegalArgumentException {
        Long userId = resolveUserId(token);
        if (userId == null) {
            throw new IllegalArgumentException("Invalid or expired token");
        }
        userService.addChatId(userId, chatId);
        tokens.remove(token);
    }

    private Long resolveUserId(String token) {
        TokenEntry entry = tokens.get(token);
        if (entry == null || entry.isExpired()) {
            return null;
        }
        return entry.userId;
    }

    private String generateToken(Long userId) {
        String token = UUID.randomUUID().toString();
        Instant expiry = Instant.now().plusSeconds(TOKEN_TTL_SECONDS);
        tokens.put(token, new TokenEntry(userId, expiry));
        return token;
    }

    private static class TokenEntry {
        final Long userId;
        final Instant expiry;

        TokenEntry(Long userId, Instant expiry) {
            this.userId = userId;
            this.expiry = expiry;
        }

        boolean isExpired() {
            return Instant.now().isAfter(expiry);
        }
    }
}
