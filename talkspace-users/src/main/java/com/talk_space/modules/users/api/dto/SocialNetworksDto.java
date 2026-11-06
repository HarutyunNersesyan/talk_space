package com.talk_space.modules.users.api.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@NoArgsConstructor
@AllArgsConstructor
@Data
public class SocialNetworksDto {
    private String userName;
    private List<SocialNetworkDto> socialNetworks;

}
