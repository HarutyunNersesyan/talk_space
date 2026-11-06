package com.talk_space.modules.users.api.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SocialNetworkDto {
    private String platform;
    private String url;
}
