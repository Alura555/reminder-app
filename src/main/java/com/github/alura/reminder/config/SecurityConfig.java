package com.github.alura.reminder.config;

import com.github.alura.reminder.security.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

    private final ClientRegistrationRepository clientRegistrationRepository;
    private final CustomOAuth2UserService oAuth2UserService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/v1/**")
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/error", "/.well-known/**").permitAll()
                        .anyRequest().authenticated()
                ).oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(oAuth2UserService))
                        .defaultSuccessUrl("/", true)
                )
                .logout(logout -> logout.logoutSuccessUrl("/login"));
        return http.build();
    }
}
