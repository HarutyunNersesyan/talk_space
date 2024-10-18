package com.talk_space.service;


import com.talk_space.model.domain.Chat;
import com.talk_space.model.domain.Like;
import com.talk_space.repository.ChatRepository;
import com.talk_space.repository.LikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Service
public class LikeService {

    private final LikeRepository likeRepository;

    private final ChatRepository chatRepository;


    @Scheduled(fixedRate = 3600000)
    public void deleteOldLikes() {
        LocalDate cutoffDate = LocalDate.now().minusDays(1);
        List<Like> oldLikes = likeRepository.findByLikeDateBefore(cutoffDate);
        likeRepository.deleteAll(oldLikes);
    }

    public Like saveLike(Like like) {
            like.setLikeDate(LocalDate.now());
            if (likeRepository.findLikeByLikerAndLiked(like.getLiker(), like.getLiked()) != null){
                chatRepository.save(new Chat(like.getLiker(), like.getLiked().getUserName()));
                chatRepository.save(new Chat(like.getLiked(), like.getLiker().getUserName()));
            }
        return likeRepository.save(like);
    }

}
