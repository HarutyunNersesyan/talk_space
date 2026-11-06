package com.talk_space.modules.feedback.internal.persistence;


import com.talk_space.modules.feedback.api.model.FeedBacks;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeedBacksRepository extends JpaRepository<FeedBacks, Long> {

    @Query("SELECT f FROM FeedBacks f WHERE f.senderUserName = :userName")
    List<FeedBacks> findFeedBacks(String userName);



}
