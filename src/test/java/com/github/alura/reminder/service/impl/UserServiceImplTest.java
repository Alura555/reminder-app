package com.github.alura.reminder.service.impl;

import com.github.alura.reminder.entity.User;
import com.github.alura.reminder.event.UserCreatedEvent;
import com.github.alura.reminder.exception.UserNotFoundException;
import com.github.alura.reminder.repository.UserRepository;
import com.github.alura.reminder.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private UserService userService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private OAuth2User oAuth2User;

    @BeforeEach
    void setup() {
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void getCurrentUser_shouldReturnUserFromRepository() {
        String email = "test@example.com";
        User expectedUser = User.builder().id(1L).email(email).build();

        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(oAuth2User);
        Mockito.when(oAuth2User.getAttribute("email")).thenReturn(email);
        Mockito.when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(expectedUser));

        User result = userService.getCurrentUser();

        Assertions.assertEquals(expectedUser, result);
    }

    @Test
    void getCurrentUser_shouldThrowExceptionWhenUserNotFound() {
        String email = "unknown@example.com";

        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(oAuth2User);
        Mockito.when(oAuth2User.getAttribute("email")).thenReturn(email);
        Mockito.when(userRepository.findUserByEmail(email)).thenReturn(Optional.empty());

        Assertions.assertThrows(UserNotFoundException.class, () -> userService.getCurrentUser());
    }

    @Test
    void getOrCreateUserFromGoogle_shouldReturnExistingUser() {
        String email = "existing@example.com";
        String login = "existingUser";
        User existingUser = User.builder().id(1L).email(email).username(login).build();

        Mockito.when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(existingUser));

        User result = userService.getOrCreateUserFromGoogle(login, email);

        Assertions.assertEquals(existingUser, result);
        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any());
        Mockito.verify(eventPublisher, Mockito.never()).publishEvent(Mockito.any());
    }

    @Test
    void getOrCreateUserFromGoogle_shouldCreateNewUserAndPublishEvent() {
        String email = "new@example.com";
        String login = "newUser";
        User savedUser = User.builder().id(2L).email(email).username(login).build();

        Mockito.when(userRepository.findUserByEmail(email)).thenReturn(Optional.empty());
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(savedUser);

        User result = userService.getOrCreateUserFromGoogle(login, email);

        Assertions.assertEquals(savedUser, result);
        Mockito.verify(userRepository).save(Mockito.any(User.class));
        Mockito.verify(eventPublisher).publishEvent(Mockito.any(UserCreatedEvent.class));
    }

    @Test
    void addChatId_shouldUpdateUserChatId() {
        Long userId = 1L;
        String chatId = "123456";
        User user = User.builder().id(userId).email("email@example.com").build();

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.addChatId(userId, chatId);

        Assertions.assertEquals(chatId, user.getTelegramChatId());
        Mockito.verify(userRepository).save(user);
    }

    @Test
    void addChatId_shouldThrowExceptionIfUserNotFound() {
        Long userId = 42L;

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Assertions.assertThrows(UserNotFoundException.class, () -> userService.addChatId(userId, "chatId"));
    }
}
