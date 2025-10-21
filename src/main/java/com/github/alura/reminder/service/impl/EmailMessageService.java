package com.github.alura.reminder.service.impl;

import com.github.alura.reminder.entity.Reminder;
import com.github.alura.reminder.entity.User;
import com.github.alura.reminder.service.AccountLinkChannel;
import com.github.alura.reminder.service.NotificationChannel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class EmailMessageService implements NotificationChannel, AccountLinkChannel {
    public static final String ACCOUNT_LINK_SUBJECT = "Приглашение в систему напоминаний";
    private final MailSender mailSender;

    @Override
    public void send(Reminder reminder) {
        send(reminder.getUser().getEmail(), reminder.getTitle(), reminder.getDescription());
    }

    @Override
    public void sendLink(User user, String invitationLink) {
        String body = String.format(
                "Здравствуйте, %s! Ваша ссылка для входа: %s",
                user.getUsername(),
                invitationLink
        );
        send(user.getEmail(), ACCOUNT_LINK_SUBJECT, body);
    }

    private void send(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent successfully to {}", to);
        } catch (MailException e) {
            log.error("Failed to send email to {}", to, e);
        }
    }
}