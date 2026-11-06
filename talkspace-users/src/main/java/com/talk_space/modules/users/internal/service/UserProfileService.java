package com.talk_space.modules.users.internal.service;

import com.talk_space.shared.error.CustomExceptions;
import com.talk_space.modules.users.api.model.User;
import com.talk_space.modules.users.api.dto.AboutMe;
import com.talk_space.modules.users.api.dto.EditUser;
import com.talk_space.modules.users.api.model.Zodiac;
import com.talk_space.modules.users.internal.persistence.UserRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.Optional;

@Service
public class UserProfileService {
    private static final Logger logger = LoggerFactory.getLogger(UserProfileService.class);
    private final UserRepository userRepository;
    public UserProfileService(UserRepository userRepository) { this.userRepository = userRepository;  }

    public User updateUserByEmail(String email, @Valid EditUser editUser) {
        logger.debug("Updating user by email: {}", email);
        Optional<User> optionalUser = userRepository.findUserByEmail(email);
        if (optionalUser.isEmpty()) {
            logger.warn("User not found for email: {}", email);
            throw new IllegalArgumentException("User with email " + email + " not found");
        }
        User user = optionalUser.get();
        LocalDate validatedBirthDate = User.validateBirthDate(editUser.getBirthDate());
        user.setZodiacSign(Zodiac.fromMonthAndDay(validatedBirthDate.getMonthValue(), validatedBirthDate.getDayOfMonth()));
        user.setFirstName(editUser.getFirstName());
        user.setLastName(editUser.getLastName());
        user.setBirthDate(validatedBirthDate);
        user.setAboutMe(editUser.getAboutMe());

        User updatedUser = userRepository.save(user);
        logger.info("User updated successfully: {}", email);
        return updatedUser;
    }

    public String updateAboutMe(AboutMe aboutMe) {
        logger.debug("Updating about me for user: {}", aboutMe.getUserName());
        Optional<User> userOptional = userRepository.findUserByUserName(aboutMe.getUserName());

        if (userOptional.isEmpty()) {
            logger.warn("User not found for about me update: {}", aboutMe.getUserName());
            throw new CustomExceptions.UserNotFoundException("User not found.");
        }
        User user = userOptional.get();

        user.setAboutMe(aboutMe.getAboutMe());

        userRepository.save(user);
        logger.info("About me updated successfully for user: {}", aboutMe.getUserName());

        return "Updated successfully";

    }
}
