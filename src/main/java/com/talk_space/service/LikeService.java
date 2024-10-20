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
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class LikeService {

    private final LikeRepository likeRepository;

    private final ChatRepository chatRepository;

    private final UserRepository userRepository;


    @Scheduled(fixedRate = 3600000)
    public void deleteOldLikes() {
        LocalDate cutoffDate = LocalDate.now().minusDays(1);
        List<Like> oldLikes = likeRepository.findByLikeDateBefore(cutoffDate);
        likeRepository.deleteAll(oldLikes);
    }

    public Like saveLike(Like like) {
        like.setLikeDate(LocalDate.now());
        Optional<User> liker = userRepository.findUserByUserName(like.getLiker().getUserName());
        Optional<User> liked = userRepository.findUserByUserName(like.getLiked().getUserName());
        like.setLiker(liker.get());
        like.setLiked(liked.get());
            if (likeRepository.findLikeByLikerUserIdAndLikedUserId(like.getLiked().getUserId(), like.getLiker().getUserId()) != null){
                chatRepository.save(new Chat(like.getLiker(), liked.get().getUserName()));
                chatRepository.save(new Chat(like.getLiked(), liker.get().getUserName()));
            }
        return likeRepository.save(like);
    }
}
