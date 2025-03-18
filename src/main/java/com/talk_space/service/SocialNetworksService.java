package com.talk_space.service;


import com.talk_space.exceptions.CustomExceptions;
import com.talk_space.model.domain.SocialNetworks;
import com.talk_space.model.domain.User;
import com.talk_space.model.dto.SocialNetworkDto;
import com.talk_space.model.dto.SocialNetworksDto;
import com.talk_space.model.dto.getters.SocialNetworksGetterDto;
import com.talk_space.repository.SocialNetworksRepository;
import com.talk_space.repository.UserRepository;
import com.talk_space.validation.SocialMediaValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SocialNetworksService {

    private final UserRepository userRepository;

    private final SocialNetworksRepository socialNetworksRepository;

    public String updateSocialNetworks(SocialNetworksDto socialNetworksDto) {

        Optional<User> userOptional = userRepository.findUserByUserName(socialNetworksDto.getUserName());
        User user = userOptional.get();
        if (userOptional.isEmpty()) {
            throw new CustomExceptions.UserNotFoundException("User not found.");
        }


        List<SocialNetworks> socialNetworks = socialNetworksRepository.getSocialNetworksByUserUserName(user.getUserName());
        socialNetworksRepository.deleteAll(socialNetworks);

        user.getSocialNetworks().clear();

        for (SocialNetworkDto socialNetworkDto : socialNetworksDto.getSocialNetworks()) {
            if (socialNetworkDto == null || !SocialMediaValidator.mediaValidation(socialNetworkDto.getPlatform(), socialNetworkDto.getUrl())) {
                throw new CustomExceptions.InvalidSocialNetworkException("Invalid platform or URL.");
            }
            SocialNetworks socialNetwork = new SocialNetworks();
            socialNetwork.setPlatform(socialNetworkDto.getPlatform());
            socialNetwork.setUrl(socialNetworkDto.getUrl());
            socialNetwork.setUser(user);
            user.getSocialNetworks().add(socialNetwork);
        }

        userRepository.save(user);
        return "Social networks updated successfully.";
    }


    public List<SocialNetworksGetterDto> getAllSocialNetworks(String userName) {
        return SocialNetworksGetterDto.socialNetworks(socialNetworksRepository.getSocialNetworksByUserUserName(userName));

    }

}
