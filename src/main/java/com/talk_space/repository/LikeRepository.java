package com.talk_space.repository;

import com.talk_space.model.domain.Like;
import com.talk_space.model.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    List<Like> findByLikeDateBefore(LocalDate cutoffDate);

    Like findLikeByLikerUserIdAndLikedUserId(Long likerId, Long likedId);

}
