package com.talk_space.modules.users.internal.service;

import com.talk_space.shared.error.CustomExceptions;
import com.talk_space.modules.users.api.model.User;
import com.talk_space.modules.users.api.dto.BlockAccount;
import com.talk_space.modules.users.api.model.Status;
import com.talk_space.modules.users.internal.persistence.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class UserModerationService {
    private static final Logger logger = LoggerFactory.getLogger(UserModerationService.class);
    private final UserRepository userRepository;
    public UserModerationService(UserRepository userRepository) { this.userRepository = userRepository;  }

    public String blockUser(BlockAccount blockAccount) {
        logger.debug("Blocking user: {}", blockAccount.getUserName());
        Optional<User> user = userRepository.findUserByUserName(blockAccount.getUserName());
        if (user.isEmpty()) {
            logger.warn("User not found for blocking: {}", blockAccount.getUserName());
            throw new CustomExceptions.UserNotFoundException("User not found.");
        }
        user.get().setStatus(Status.BLOCKED);
        user.get().setBlockedMessage(blockAccount.getBlockMessage());
        user.get().setUntilBlockedDate(blockAccount.getBlockUntil());
        userRepository.save(user.get());
        logger.info("User blocked: {} until {}", blockAccount.getUserName(), blockAccount.getBlockUntil());
        return "User blocked until " + blockAccount.getBlockUntil() + ". Reason: " + blockAccount.getBlockMessage();
    }

    public String unblockUser(String username) {
        logger.debug("Unblocking user: {}", username);
        Optional<User> user = userRepository.findUserByUserName(username);
        if (user.isEmpty()) {
            logger.warn("User not found for unblocking: {}", username);
            throw new CustomExceptions.UserNotFoundException("User not found.");
        }
        user.get().setStatus(Status.ACTIVE);
        user.get().setBlockedMessage(null);
        user.get().setUntilBlockedDate(null);
        userRepository.save(user.get());
        logger.info("User unblocked: {}", username);
        return "User has been unblocked successfully";
    }
}
