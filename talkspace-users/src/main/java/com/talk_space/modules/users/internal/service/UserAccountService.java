package com.talk_space.modules.users.internal.service;

import com.talk_space.shared.error.CustomExceptions;
import com.talk_space.modules.users.api.model.SocialNetworks;
import com.talk_space.modules.users.api.model.User;
import com.talk_space.modules.users.api.dto.DeleteAccount;
import com.talk_space.modules.users.internal.persistence.SocialNetworksRepository;
import com.talk_space.modules.users.internal.persistence.UserRepository;
import com.talk_space.modules.users.api.ImageService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UserAccountService {
    private static final Logger logger = LoggerFactory.getLogger(UserAccountService.class);
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final ImageService imageService;
    private final SocialNetworksRepository socialNetworksRepository;
    private final org.springframework.context.ApplicationEventPublisher events;
    public UserAccountService(UserRepository userRepository, @Lazy AuthenticationManager authenticationManager,
            ImageService imageService, SocialNetworksRepository socialNetworksRepository,
            org.springframework.context.ApplicationEventPublisher events) {
        this.userRepository=userRepository; this.authenticationManager=authenticationManager;
        this.imageService=imageService; this.socialNetworksRepository=socialNetworksRepository; this.events=events;
    }

    @Transactional
    public void delete(DeleteAccount deleteAccount) {
        logger.debug("Attempting to delete account for email: {}", deleteAccount.getEmail());
        Optional<User> userOptional = userRepository.findUserByEmail(deleteAccount.getEmail());

        if (userOptional.isEmpty()) {
            logger.warn("User not found for deletion: {}", deleteAccount.getEmail());
            throw new CustomExceptions.UserNotFoundException("User not found.");
        }

        User user = userOptional.get();
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(), deleteAccount.getPassword()));
        } catch (Exception e) {
            logger.warn("Invalid password during account deletion for: {}", deleteAccount.getEmail());
            throw new CustomExceptions.InvalidPassword("Invalid password");
        }
        if (user.getImage() != null) {
            imageService.deleteUserImage(user.getUserName());
        }
        if (!user.getSpecialities().isEmpty()) {
            user.getSpecialities().clear();
        }
        if (!user.getHobbies().isEmpty()) {
            user.getHobbies().clear();
        }
        List<SocialNetworks> socialNetworks = socialNetworksRepository.getSocialNetworksByUserUserName(user.getUserName());
        if (!socialNetworks.isEmpty()) {
            socialNetworksRepository.deleteAll(socialNetworks);
        }
        events.publishEvent(new com.talk_space.modules.users.api.UserDeleting(user.getUserName(), user.getEmail()));
        userRepository.delete(user);
        logger.info("Account deleted successfully for: {}", deleteAccount.getEmail());
    }

    @Transactional
    public void deleteVerify(String email) {
        logger.debug("Deleting unverified user: {}", email);
        Optional<User> userOptional = userRepository.findUserByEmail(email);

        if (userOptional.isEmpty()) {
            logger.warn("User not found for deletion: {}", email);
            throw new CustomExceptions.UserNotFoundException("User not found.");
        }

        if (Boolean.TRUE.equals(userOptional.get().getVerifyMail())) {
            throw new IllegalArgumentException("Verified accounts cannot be deleted through verification cancellation.");
        }
        events.publishEvent(new com.talk_space.modules.users.api.UserDeleting(userOptional.get().getUserName(), email));
        userRepository.delete(userOptional.get());
        logger.info("Unverified user deleted: {}", email);
    }
}
