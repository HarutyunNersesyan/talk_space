package com.talk_space.modules.users.internal.service;

import com.talk_space.shared.error.CustomExceptions;
import com.talk_space.modules.users.api.model.User;
import com.talk_space.modules.users.api.dto.ForgotPassword;
import com.talk_space.modules.users.internal.persistence.UserRepository;
import com.talk_space.modules.users.api.MailSenderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class UserPasswordService {
    private static final Logger logger = LoggerFactory.getLogger(UserPasswordService.class);
    private final UserRepository userRepository; private final AuthenticationManager authenticationManager; private final PasswordEncoder passwordEncoder; private final MailSenderService mailSenderService;
    public UserPasswordService(UserRepository userRepository, @Lazy AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder, MailSenderService mailSenderService) { this.userRepository = userRepository; this.authenticationManager = authenticationManager; this.passwordEncoder = passwordEncoder; this.mailSenderService = mailSenderService;  }

    public String changePassword(String email, String oldPassword, String newPassword, String newPasswordRepeat) {
        logger.debug("Attempting password change for user: {}", email);

        Optional<User> optionalUser = userRepository.findUserByEmail(email);
        User user = optionalUser.get();

        if (!user.getVerifyMail()) {
            logger.warn("Password change attempted for unverified email: {}", email);
            throw new CustomExceptions.NotVerifiedMailException("Mail must be verified");
        }

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, oldPassword));
        } catch (Exception e) {
            logger.warn("Invalid old password for user: {}", email);
            throw new CustomExceptions.InvalidOldPasswordException("Old password is incorrect.");
        }

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            logger.warn("New password same as current for user: {}", email);
            throw new CustomExceptions.InvalidNewPasswordException("New password cannot be the same as the current password.");
        }

        if (!newPassword.equals(newPasswordRepeat)) {
            logger.warn("Password mismatch for user: {}", email);
            throw new CustomExceptions.PasswordMismatchException("New passwords do not match.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        logger.info("Password changed successfully for user: {}", email);

        return "Password updated successfully.";
    }

    public String forgotPassword(ForgotPassword forgotPassword) {
        logger.debug("Processing forgot password for: {}", forgotPassword.getEmail());
        Optional<User> optionalUser = userRepository.findUserByEmail(forgotPassword.getEmail());
        User existingUser = optionalUser.get();

        if (!existingUser.getVerifyMail()) {
            logger.warn("Forgot password attempted for unverified email: {}", forgotPassword.getEmail());
            throw new CustomExceptions.NotVerifiedMailException("Mail must be verified");
        }
        String newPassword = forgotPassword.generatePassword();
        existingUser.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(existingUser);
        mailSenderService.sendEmail(forgotPassword.getEmail(), "New password", newPassword);
        logger.info("New password sent to email: {}", forgotPassword.getEmail());
        return "New password has been send to email";
    }
}
