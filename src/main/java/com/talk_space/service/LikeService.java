package com.talk_space.service;


import com.talk_space.model.domain.ChatMessage;
import com.talk_space.model.domain.Like;
import com.talk_space.model.domain.User;
import com.talk_space.repository.ChatRepository;
import com.talk_space.repository.LikeRepository;
import com.talk_space.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
@Service
public class LikeService {

    private final LikeRepository likeRepository;

    private final ChatRepository chatRepository;

    private final UserRepository userRepository;


    @Scheduled(fixedRate = 3600000)
    public void deleteOldLikes() {
        LocalDate cutoffDate = LocalDate.now()
                .minusDays(1);
        List<Like> oldLikes = likeRepository.findByLikeDateBefore(cutoffDate);
        likeRepository.deleteAll(oldLikes);
    }

    public Like saveLike(Like like) {
        // Validate input
        if (like == null || like.getLiker() == null || like.getLiked() == null) {
            throw new IllegalArgumentException("Like object and user references cannot be null");
        }

        // Set current date
        like.setLikeDate(LocalDate.now());

        // Fetch complete user objects
        User liker = userRepository.findUserByUserName(like.getLiker().getUserName())
                .orElseThrow(() -> new NoSuchElementException("Liker user not found"));
        User liked = userRepository.findUserByUserName(like.getLiked().getUserName())
                .orElseThrow(() -> new NoSuchElementException("Liked user not found"));

        // Set the complete user objects
        like.setLiker(liker);
        like.setLiked(liked);

        // Check for existing like in both directions
        boolean reverseLikeExists = likeRepository.existsByLikerUserNameAndLikedUserName(
                liked.getUserName(),
                liker.getUserName()
        );

        boolean sameLikeExists = likeRepository.existsByLikerUserNameAndLikedUserName(
                liker.getUserName(),
                liked.getUserName()
        );

        // If mutual like detected and not already processed
        if (reverseLikeExists && !sameLikeExists) {
            // Create mutual like notification messages
            ChatMessage messageToLiker = new ChatMessage();
            messageToLiker.setSender(liked);
            messageToLiker.setReceiver(liker);
            messageToLiker.setContent("You have a mutual liking with " + liked.getFirstName());

            ChatMessage messageToLiked = new ChatMessage();
            messageToLiked.setSender(liker);
            messageToLiked.setReceiver(liked);
            messageToLiked.setContent("You have a mutual liking with " + liker.getFirstName());

            chatRepository.saveAll(List.of(messageToLiker, messageToLiked));
        }

        return likeRepository.save(like);
    }
}
