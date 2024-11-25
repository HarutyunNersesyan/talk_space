package com.talk_space.service;


import com.talk_space.model.domain.SocialNetworks;
import com.talk_space.model.domain.User;
import com.talk_space.model.dto.SocialNetworksDto;
import com.talk_space.repository.SocialNetworksRepository;
import com.talk_space.repository.UserRepository;
import com.talk_space.validation.SocialMediaValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SocialNetworksService {

    private final SocialNetworksRepository socialNetworksRepository;

    private final UserRepository userRepository;

    private final SocialMediaValidator socialMediaValidator;


    public ResponseEntity<String> addSocialNetworks(SocialNetworksDto sn) {
        Optional<User> userOptional = userRepository.findUserByUserName(sn.getUserName());


        User user = userOptional.get();
        List<SocialNetworks> validSocialNetworks = new ArrayList<>();

        for (SocialNetworks socialNetwork : sn.getSocialNetworks()) {
            if (socialNetwork != null &&
                    SocialMediaValidator.mediaValidation(socialNetwork.getPlatform(), socialNetwork.getUrl())) {
                socialNetwork.setUser(user);
                validSocialNetworks.add(socialNetwork);
            }
        }


        if (!validSocialNetworks.isEmpty()) {
            user.setSocialNetwork(validSocialNetworks);
            userRepository.save(user);
            return ResponseEntity.ok("Social networks added successfully");
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No valid social networks to add");
    }


}
