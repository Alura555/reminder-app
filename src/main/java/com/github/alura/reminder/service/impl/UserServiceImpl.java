package com.github.alura.reminder.service.impl;

import com.github.alura.reminder.entity.User;
import com.github.alura.reminder.event.UserCreatedEvent;
import com.github.alura.reminder.exception.UserNotFoundException;
import com.github.alura.reminder.repository.UserRepository;
import com.github.alura.reminder.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication.getPrincipal() instanceof OAuth2User oAuth2User)) {
            throw new IllegalStateException("Invalid authentication principal");
        }

        String email = oAuth2User.getAttribute(StandardClaimNames.EMAIL);

        return userRepository.findUserByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));

    }

    @Override
    public User getOrCreateUserFromGoogle(String login, String email) {
        return userRepository.findUserByEmail(email)
                .orElseGet(() -> {
                    User user = User.builder()
                            .username(login)
                            .email(email)
                            .build();
                    user = userRepository.save(user);

                    eventPublisher.publishEvent(new UserCreatedEvent(user));

                    return user;
                });
    }

    @Override
    public void addChatId(Long userId, String chatId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        user.setTelegramChatId(chatId);
        userRepository.save(user);
    }
}
