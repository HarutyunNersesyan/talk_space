package com.talk_space.repository;

import com.talk_space.model.domain.Like;
import com.talk_space.model.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {


    boolean existsByLikerUserNameAndLikedUserName(String likerUserName, String likedUserName);



    @Query(value = "SELECT liked FROM user_likes WHERE liker = :userName", nativeQuery = true)
    Set<String> findLikesByLiker(@Param("userName") String userName);


}
