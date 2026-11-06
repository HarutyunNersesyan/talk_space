package com.talk_space.modules.users.internal.persistence;

import com.talk_space.modules.users.api.model.Image;
import com.talk_space.modules.users.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

    Optional<Image> findByUserUserName(String usrName);


    Optional<Image> findByUserAndFileName(User user, String fileName);


}
