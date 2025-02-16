package com.talk_space.model.dto.getters;


import com.talk_space.model.domain.SocialNetworks;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SocialNetworksGetterDto {

    private String userName;

    private String platform;

    private String url;

    public static List<SocialNetworksGetterDto> socialNetworks(List<SocialNetworks> socialNetworks) {
        List<SocialNetworksGetterDto> socialNetworksDtos = new ArrayList<>();

        for (int i = 0; i < socialNetworks.size(); i++) {
            socialNetworksDtos.add(new SocialNetworksGetterDto(socialNetworks.get(i).getUser().getUserName(),
                    socialNetworks.get(i).getPlatform(),
                    socialNetworks.get(i).getUrl()));
        }
        return socialNetworksDtos;
    }
}
