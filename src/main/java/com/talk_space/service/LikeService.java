package com.talk_space.service;


import com.talk_space.model.domain.ChatMessage;
import com.talk_space.model.domain.Like;
import com.talk_space.model.domain.User;
import com.talk_space.repository.ChatMessageRepository;
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

    private final ChatMessageRepository chatRepository;

    private final UserRepository userRepository;


    @Scheduled(fixedRate = 3600000)
    public void deleteOldLikes() {
        LocalDate cutoffDate = LocalDate.now()
                .minusDays(1);
        List<Like> oldLikes = likeRepository.findByLikeDateBefore(cutoffDate);
        likeRepository.deleteAll(oldLikes);
    }

    public Like saveLike(Like like) {
        like.setLikeDate(LocalDate.now());

        User liker = userRepository.findUserByUserName(like.getLiker()
                        .getUserName())
                .orElse(null);
        User liked = userRepository.findUserByUserName(like.getLiked()
                        .getUserName())
                .orElse(null);

        if (liker == null || liked == null) {
            throw new NoSuchElementException("User not found");
        }

        like.setLiker(liker);
        like.setLiked(liked);

        if (likeRepository.existsByLikerUserIdAndLikedUserId(liked.getUserId(), liker.getUserId()) &&
               !likeRepository.existsByLikerUserIdAndLikedUserId(liker.getUserId(), liked.getUserId())) {
            chatRepository.saveAll(List.of(
                    new ChatMessage(liker, liked.getUserName(),"You have a mutual liking", LocalDateTime.now()),
            new ChatMessage(liked, liker.getUserName(),"You have a mutual liking", LocalDateTime.now())
            ));
        }
        return likeRepository.save(like);
    }


}
