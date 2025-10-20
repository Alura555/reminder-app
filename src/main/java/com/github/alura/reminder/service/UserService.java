package com.github.alura.reminder.service;

import com.github.alura.reminder.entity.User;
import com.github.alura.reminder.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute(StandardClaimNames.EMAIL);

        return userRepository.findUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

    }

    public User getOrCreateUserFromGoogle(String login, String email) {
        return userRepository.findUserByEmail(email)
                .orElseGet(() -> {
                    User user = User.builder()
                            .username(login)
                            .email(email)
                            .build();
                    return userRepository.save(user);
                });
    }
}
