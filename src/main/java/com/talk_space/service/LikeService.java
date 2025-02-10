package com.talk_space.service;


import com.talk_space.model.domain.Chat;
import com.talk_space.model.domain.Like;
import com.talk_space.model.domain.User;
import com.talk_space.repository.ChatRepository;
import com.talk_space.repository.LikeRepository;
import com.talk_space.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

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

        if (likeRepository.existsByLikerUserIdAndLikedUserId(liked.getUserId(), liker.getUserId())) {
            chatRepository.saveAll(List.of(
                    new Chat(liker, liked.getUserName()),
                    new Chat(liked, liker.getUserName())
            ));
        }

        return likeRepository.save(like);
    }


}
