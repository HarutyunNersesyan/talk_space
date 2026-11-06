package com.talk_space.modules.users.internal.persistence;


import com.talk_space.modules.users.api.model.SocialNetworks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SocialNetworksRepository extends JpaRepository<SocialNetworks, Long> {

        List<SocialNetworks> getSocialNetworksByUserUserName(String userName);


}
