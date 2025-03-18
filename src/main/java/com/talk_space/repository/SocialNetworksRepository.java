package com.talk_space.repository;


import com.talk_space.model.domain.SocialNetworks;
import com.talk_space.model.domain.Speciality;
import com.talk_space.model.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SocialNetworksRepository extends JpaRepository<SocialNetworks, Long> {

        List<SocialNetworks> getSocialNetworksByUserUserName(String userName);


}
