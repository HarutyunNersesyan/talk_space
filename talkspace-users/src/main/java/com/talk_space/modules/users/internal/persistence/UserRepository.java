package com.talk_space.modules.users.internal.persistence;


import com.talk_space.modules.users.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findUserByEmail(String email);


    Optional<User> findUserByUserName(String userName);


    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.userName = :userName")
    Optional<User> lockMatchingViewer(@Param("userName") String userName);



    java.util.List<User> findByUserNameInAndStatusAndRoleAndVerifyMailTrue(
            java.util.Collection<String> names,
            com.talk_space.modules.users.api.model.Status status,
            com.talk_space.modules.users.api.model.Role role);
}
