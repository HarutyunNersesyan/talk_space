package com.talk_space.repository;

import com.talk_space.model.domain.Image;
import com.talk_space.model.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    List<Image> findByUserUserName(String userName);

    List<Image> findByUser(User user);

    Optional<Image> findByUserAndFileName(User user, String fileName);


}
