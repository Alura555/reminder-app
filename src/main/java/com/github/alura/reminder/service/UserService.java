package com.github.alura.reminder.service;

import com.github.alura.reminder.entity.User;
import com.github.alura.reminder.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private UserRepository userRepository;

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findUserByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

    }
}
