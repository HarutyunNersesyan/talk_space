package com.talk_space.repository;


import com.talk_space.model.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findUserByEmail(String email);

    void deleteUserByPassword(String password);

    Optional<User> findUserByUserName(String userName);




//    @Transactional
//    @Query("SELECT new com.talk_space.model.dto.UserBasicInfo(u.userName, u.birthDate, u.location, u.images, u.hobbies) " +
//            "FROM User u WHERE u.status = 'ACTIVE'")
//    List<UserBasicInfo> getBasicInfo();
}
