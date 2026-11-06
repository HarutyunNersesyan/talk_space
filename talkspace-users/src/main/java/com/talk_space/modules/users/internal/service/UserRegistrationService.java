package com.talk_space.modules.users.internal.service;

import com.talk_space.shared.error.CustomExceptions;
import com.talk_space.modules.users.api.model.User;
import com.talk_space.modules.users.api.dto.Verify;
import com.talk_space.modules.users.api.model.Role;
import com.talk_space.modules.users.api.model.Status;
import com.talk_space.modules.users.api.model.Zodiac;
import com.talk_space.modules.users.internal.persistence.UserRepository;
import com.talk_space.shared.logging.LoggingUtil;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.Optional;

@Service
public class UserRegistrationService {
    private static final Logger logger = LoggerFactory.getLogger(UserRegistrationService.class);
    private final UserRepository userRepository; private final PasswordEncoder passwordEncoder;
    public UserRegistrationService(UserRepository userRepository, PasswordEncoder passwordEncoder) { this.userRepository = userRepository; this.passwordEncoder = passwordEncoder;  }

    public User signUp(@Valid User user) {
        LoggingUtil.logInfo(UserRegistrationService.class, "Starting user signup for email: {}", user.getEmail());

        try {
            user.setCreatedDate(LocalDate.now());
            user.setRole(Role.USER);
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setZodiacSign(Zodiac.fromMonthAndDay(user.getBirthDate().getMonthValue(), user.getBirthDate().getDayOfMonth()));
            user.setVerifyMail(false);
            user.setStatus(Status.ACTIVE);

            User savedUser = userRepository.save(user);

            LoggingUtil.logUserAction(user.getUserName(), "SIGNUP", "User registered successfully");
            logger.info("User signup completed successfully for: {}", user.getEmail());

            return savedUser;

        } catch (Exception e) {
            LoggingUtil.logError(UserRegistrationService.class, "Error during user signup for email: {}", user.getEmail(), e);
            throw e;
        }
    }

    public String verify(Verify verify) {
        logger.debug("Verifying email: {}", verify.getEmail());
        Optional<User> user = userRepository.findUserByEmail(verify.getEmail());
        if (user.get().getVerifyMail()) {
            logger.warn("Email already verified: {}", verify.getEmail());
            throw new CustomExceptions.AlreadyVerifiedEmail("Your email is already verified.");
        }
        if (!user.get().getPin().equals(verify.getPin())) {
            logger.warn("Invalid PIN for email: {}", verify.getEmail());
            throw new CustomExceptions.InvalidPinExceptions("Invalid PIN.");
        }
        User existingUser = user.get();
        existingUser.setVerifyMail(true);
        existingUser.setPin(null);
        userRepository.save(existingUser);
        logger.info("Email verified successfully: {}", verify.getEmail());
        return "Mail verified successfully";
    }
}
