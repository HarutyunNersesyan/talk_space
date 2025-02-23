package com.talk_space.service;


import com.talk_space.exceptions.CustomExceptions;
import com.talk_space.model.domain.User;
import com.talk_space.model.dto.ForgotPassword;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class MailSenderService {

    private final JavaMailSender mailSender;

    private final UserService userService;

    private final ForgotPassword forgotPassword;

    @Value("$(spring.email.username)")
    private String fromMail;

    public void sendEmail(String mail, String subject, String message) {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(fromMail);
        simpleMailMessage.setSubject(subject);
        simpleMailMessage.setText(message);
        simpleMailMessage.setTo(mail);
        mailSender.send(simpleMailMessage);
    }

    public String handlePinRequest(String mail, boolean checkVerifyMail) {

        User user = userService.findUserByEmail(mail)
                .orElseThrow(() -> new CustomExceptions.UserNotFoundException("User not found with email: " + mail));

        if (checkVerifyMail && !user.getVerifyMail()) {
            throw new CustomExceptions.UserNotActiveException("User is not active. Please verify your email.");
        }

        String pin = forgotPassword.generatePin();
        user.setPin(pin);
        userService.update(user);

        sendEmail(mail, "Verify code", pin);

        schedulePinReset(mail);

        return "A verification code has been sent to your email.";
    }


    public void schedulePinReset(String mail) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.schedule(() -> {
            userService.findUserByEmail(mail)
                    .ifPresent(user -> {
                        user.setPin(null);
                        userService.update(user);
                    });
        }, 30, TimeUnit.MINUTES);
    }
}
