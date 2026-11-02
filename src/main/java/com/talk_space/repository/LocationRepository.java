package com.talk_space.repository;

import com.talk_space.model.domain.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    Optional<Location> findByUserUserName(String userName);
    void deleteByUserUserName(String userName);
}