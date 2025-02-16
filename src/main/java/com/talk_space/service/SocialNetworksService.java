package com.talk_space.service;


import com.talk_space.exceptions.CustomExceptions;
import com.talk_space.model.domain.SocialNetworks;
import com.talk_space.model.domain.User;
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

    public String addSocialNetworks(SocialNetworksDto sn) throws CustomExceptions.InvalidSocialNetworkException {
        Optional<User> userOptional = userRepository.findUserByUserName(sn.getUserName());
        if (userOptional.isEmpty()) {
            throw new CustomExceptions.UserNotFoundException("User not found with username: " + sn.getUserName());
        }

        User user = userOptional.get();
        List<SocialNetworks> validSocialNetworks = new ArrayList<>();
        for (SocialNetworks socialNetwork : sn.getSocialNetworks()) {
            if (socialNetwork != null && SocialMediaValidator.mediaValidation(socialNetwork.getPlatform(), socialNetwork.getUrl())) {
                socialNetwork.setUser(user);
                validSocialNetworks.add(socialNetwork);
            }
        }

        if (validSocialNetworks.isEmpty()) {
            throw new CustomExceptions.InvalidSocialNetworkException("No valid social networks to add.");
        }

        user.getSocialNetwork()
                .clear();
        user.getSocialNetwork()
                .addAll(validSocialNetworks);
        userRepository.save(user);

        return "Social networks added successfully.";
    }
    public List<SocialNetworksGetterDto> getAllSocialNetworks(String userName){
           return SocialNetworksGetterDto.socialNetworks(socialNetworksRepository.getSocialNetworksByUserUserName(userName));

    }

}
