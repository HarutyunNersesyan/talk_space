package com.talk_space.repository;


import com.talk_space.model.domain.User;
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

    @Query(value = """
        select distinct u.* from user_entity u join user_connections c
          on (c.sender = :userName and u.user_name = c.receiver)
          or (c.receiver = :userName and u.user_name = c.sender)
        where c.status = 'ACCEPTED' and u.status = 'ACTIVE' and u.role='USER' and u.verify_mail=true
          and not exists (select 1 from user_blocks b where (b.blocker=:userName and b.blocked=u.user_name)
            or (b.blocker=u.user_name and b.blocked=:userName))
        """, nativeQuery = true)
    List<User> findConnectionPartners(@Param("userName") String userName);

}
