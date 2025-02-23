package com.talk_space.repository;


import com.talk_space.model.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findUserByEmail(String email);



    Optional<User> findUserByUserName(String userName);


//    @Query(value = """
//    SELECT * FROM User c
//    WHERE
//    AND (c.is_blocked = false OR c.is_blocked  IS NULL)
//    AND c.looking_for = (SELECT gender FROM customer WHERE username = :currentUsername)
//    AND c.username NOT IN (
//        SELECT l.to_username FROM "likes" l WHERE l.from_username = :currentUsername
//    )
//    AND c.username != :currentUsername
//    ORDER BY RANDOM()
//    LIMIT 1
//""", nativeQuery = true)
//    Optional<User> findRandomUsers(String currentUsername, String hobbyName);



//    @Query(value = """
//    SELECT  u.* FROM user_entity u
//    LEFT JOIN user_hobbies uh ON u.user_name = uh.user_name
//    LEFT JOIN hobby h ON uh.hobby_name = h.name
//    WHERE h.name IN (:hobbyNames)
//       OR h.parent_id IN (:parentIds)
//       OR NOT EXISTS (
//           SELECT 1 FROM user_hobbies uh2
//           JOIN hobby h2 ON uh2.hobby_name = h2.name
//           WHERE uh2.user_name = u.user_name
//             AND (h2.name IN (:hobbyNames) OR h2.parent_id IN (:parentIds))
//       )
//    ORDER BY
//        CASE
//            WHEN h.name IN (:hobbyNames) THEN 1
//            WHEN h.parent_id IN (:parentIds) THEN 2
//            ELSE 3
//        END,
//        RANDOM()
//    LIMIT 1
//""", nativeQuery = true)

    @Query(value = """
    SELECT DISTINCT u.*
    FROM user_entity u
    JOIN user_hobbies uh ON u.user_name = uh.user_name
    JOIN hobby h ON uh.hobby_name = h.name
    LEFT JOIN hobby h2 ON h2.name = uh.hobby_name
    WHERE u.status = 'ACTIVE'
    AND (
        h.name IN (SELECT uh2.hobby_name FROM user_hobbies uh2 WHERE uh2.user_name = :userName)
        OR h.parent_id IN (SELECT h3.parent_id FROM hobby h3 
                           JOIN user_hobbies uh3 ON uh3.hobby_name = h3.name
                           WHERE uh3.user_name = :userName AND h3.parent_id IS NOT NULL)
    )
    ORDER BY u.user_name
""",
            countQuery = """
    SELECT COUNT(DISTINCT u.user_name)
    FROM user_entity u
    JOIN user_hobbies uh ON u.user_name = uh.user_name
    JOIN hobby h ON uh.hobby_name = h.name
    LEFT JOIN hobby h2 ON h2.name = uh.hobby_name
    WHERE u.status = 'ACTIVE'
    AND u.user_name <> :userName
    AND (
        h.name IN (SELECT uh2.hobby_name FROM user_hobbies uh2 WHERE uh2.user_name = :userName)
        OR h.parent_id IN (SELECT h3.parent_id FROM hobby h3 
                           JOIN user_hobbies uh3 ON uh3.hobby_name = h3.name
                           WHERE uh3.user_name = :userName AND h3.parent_id IS NOT NULL)
    )
""",
            nativeQuery = true
    )
    Page<User> findUsersByHobbies(@Param("userName") String userName, Pageable pageable);


    @Query(value = """
    SELECT DISTINCT u.*
    FROM user_entity u
    JOIN user_specialities us ON u.user_name = us.user_name
    JOIN speciality s ON us.speciality_name = s.name
    LEFT JOIN speciality s2 ON s2.name = us.speciality_name
    WHERE u.status = 'ACTIVE'
    AND u.user_name <> :userName
    AND (
        s.name IN (SELECT us2.speciality_name FROM user_specialities us2 WHERE us2.user_name = :userName)
        OR s.parent_id IN (SELECT s3.parent_id FROM speciality s3 
                           JOIN user_specialities us3 ON us3.speciality_name = s3.name
                           WHERE us3.user_name = :userName AND s3.parent_id IS NOT NULL)
    )
    ORDER BY u.user_name
""",
            countQuery = """
    SELECT COUNT(DISTINCT u.user_name)
    FROM user_entity u
    JOIN user_specialities us ON u.user_name = us.user_name
    JOIN speciality s ON us.speciality_name = s.name
    LEFT JOIN speciality s2 ON s2.name = us.speciality_name
    WHERE u.status = 'ACTIVE'
    AND u.user_name <> :userName
    AND (
        s.name IN (SELECT us2.speciality_name FROM user_specialities us2 WHERE us2.user_name = :userName)
        OR s.parent_id IN (SELECT s3.parent_id FROM speciality s3 
                           JOIN user_specialities us3 ON us3.speciality_name = s3.name
                           WHERE us3.user_name = :userName AND s3.parent_id IS NOT NULL)
    )
""",
            nativeQuery = true
    )
    Page<User> findUsersBySpecialities(@Param("userName") String userName, Pageable pageable);










}
