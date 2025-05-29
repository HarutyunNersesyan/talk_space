package com.talk_space.repository;


import com.talk_space.model.domain.LikesCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LikesCountRepository extends JpaRepository<LikesCount, Long> {

    @Query(value = "SELECT * FROM likes_count ORDER BY count DESC LIMIT 10", nativeQuery = true)
    List<LikesCount> findTop10LikesCounts();



}
