package com.talk_space.api.controller.publics;


import com.talk_space.model.domain.User;
import com.talk_space.model.dto.ForgotPassword;
import com.talk_space.service.EmailSenderService;
import com.talk_space.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/public/email")
@RequiredArgsConstructor
public class PublicEmailSenderController {


    private final UserService userService;

    private final EmailSenderService emailSenderService;

    private final ForgotPassword forgotPassword;

    @PutMapping("/pin")
    public ResponseEntity<?> savePin(@RequestParam String email) {
        Optional<User> userOptional = userService.findUserByEmail(email);

        if (userOptional.isEmpty()) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }

        User user = userOptional.get();

        if (!user.getIsActive()) {
            return new ResponseEntity<>("User is not active", HttpStatus.BAD_REQUEST);
        }

        String pin = forgotPassword.generatePin();
        user.setPin(pin);
        userService.save(user);

        emailSenderService.sendEmail(email, "Verify code", pin);

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.schedule(() -> {
            Optional<User> userToDeletePin = userService.findUserByEmail(email);
            if (userToDeletePin.isPresent()) {
                User u = userToDeletePin.get();
                u.setPin(null);
                userService.save(u);
            }
        }, 30, TimeUnit.MINUTES);

        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @PutMapping("/active")
    public ResponseEntity<?> savePinForActive(@RequestParam String email) {
        Optional<User> userOptional = userService.findUserByEmail(email);

        if (userOptional.isEmpty()) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }

        User user = userOptional.get();

        String pin = forgotPassword.generatePin();
        user.setPin(pin);
        userService.save(user);

        emailSenderService.sendEmail(email, "Verify code", pin);
        if (!user.getIsActive()) {
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            scheduler.schedule(() -> {
                Optional<User> userToDeletePin = userService.findUserByEmail(email);
                if (userToDeletePin.isPresent()) {
                    User u = userToDeletePin.get();
                    u.setPin(null);
                    userService.save(u);
                }
            }, 30, TimeUnit.MINUTES);
        }
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }


}
