package com.talk_space.model.dto;


import com.talk_space.model.domain.Hobby;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class HobbyDto {

    private String userName;

    private List<Hobby> hobbies;
}
