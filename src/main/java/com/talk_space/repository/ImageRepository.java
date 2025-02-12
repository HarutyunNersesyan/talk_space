package com.talk_space.repository;

import com.talk_space.model.domain.Image;
import com.talk_space.model.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    List<Image> findByUserUserName(String userName);

    List<Image> findByUser(User user);
}
