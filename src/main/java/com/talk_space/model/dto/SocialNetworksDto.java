package com.talk_space.model.dto;


import com.talk_space.model.domain.SocialNetworks;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Component
public class SocialNetworksDto {

    private String userName;

    private List<SocialNetworks> socialNetworks;
}
