package com.talk_space.service;


import com.talk_space.model.domain.ChatMessage;
import com.talk_space.model.domain.Like;
import com.talk_space.model.domain.User;
import com.talk_space.repository.ChatRepository;
import com.talk_space.repository.LikeRepository;
import com.talk_space.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class LikeService {

    private final LikeRepository likeRepository;

    private final ChatRepository chatRepository;

    private final UserRepository userRepository;


    public Boolean getLike(String liker, String liked) {
        boolean reverseLikeExists = likeRepository.existsByLikerUserNameAndLikedUserName(
                liker,
                liked);
        return reverseLikeExists;

    }

    public Like saveLike(String likerDto, String likedDto) {
        if (likerDto == null || likedDto == null) {
            throw new IllegalArgumentException("Like object and user references cannot be null");
        }

        User liker = userRepository.findUserByUserName(likerDto)
                .orElseThrow(() -> new NoSuchElementException("Liker user not found"));
        User liked = userRepository.findUserByUserName(likedDto)
                .orElseThrow(() -> new NoSuchElementException("Liked user not found"));

        // Check if like already exists
        if (likeRepository.existsByLikerUserNameAndLikedUserName(liker.getUserName(), liked.getUserName())) {
            throw new DataIntegrityViolationException("Like already exists");
        }

        Like like = new Like(liker, liked);

        // Check for existing like in reverse direction
        boolean reverseLikeExists = likeRepository.existsByLikerUserNameAndLikedUserName(
                liked.getUserName(),
                liker.getUserName()
        );

        // If mutual like detected
        if (reverseLikeExists) {
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
