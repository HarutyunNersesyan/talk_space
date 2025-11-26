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


    @Query(value = """
                SELECT u.* 
                FROM user_entity u
                JOIN user_hobbies us ON u.user_name = us.user_name
                JOIN hobby s ON us.hobby_name = s.name
                WHERE u.status = 'ACTIVE'
                AND u.user_name != :userName
                AND (:excludedUserNames IS NULL OR u.user_name NOT IN (:excludedUserNames))
                AND (
                    s.name IN (SELECT hobby_name FROM user_hobbies WHERE user_name = :userName)
                    OR s.parent_id IN (
                        SELECT parent_id FROM hobby 
                        WHERE name IN (
                            SELECT hobby_name FROM user_hobbies WHERE user_name = :userName
                        ) AND parent_id IS NOT NULL
                    )
                )
                ORDER BY RANDOM()
                LIMIT 1
            """, nativeQuery = true)
    Optional<User> findNextUserByHobbies(
            @Param("userName") String userName,
            @Param("excludedUserNames") Set<String> excludedUserNames
    );


    @Query(value = """
                SELECT u.* 
                FROM user_entity u
                JOIN user_specialities us ON u.user_name = us.user_name
                JOIN speciality s ON us.speciality_name = s.name
                WHERE u.status = 'ACTIVE'
                AND u.user_name != :userName
                AND (:excludedUserNames IS NULL OR u.user_name NOT IN (:excludedUserNames))
                AND (
                    s.name IN (SELECT speciality_name FROM user_specialities WHERE user_name = :userName)
                    OR s.parent_id IN (SELECT parent_id FROM speciality WHERE name IN 
                                       (SELECT speciality_name FROM user_specialities WHERE user_name = :userName) 
                                       AND parent_id IS NOT NULL)
                )
                ORDER BY RANDOM()
                LIMIT 1
            """, nativeQuery = true)
    Optional<User> findNextUserBySpecialities(
            @Param("userName") String userName,
            @Param("excludedUserNames") Set<String> excludedUserNames
    );




    @Query("SELECT DISTINCT l.liker FROM Like l " +
            "WHERE l.liked.userName = :userName " +
            "AND EXISTS (SELECT l2 FROM Like l2 WHERE l2.liker.userName = :userName AND l2.liked = l.liker)")
    List<User> findMutualLikePartners(@Param("userName") String userName);

}
