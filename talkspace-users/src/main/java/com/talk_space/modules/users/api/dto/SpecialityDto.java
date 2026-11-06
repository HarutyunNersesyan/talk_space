package com.talk_space.modules.users.api.dto;


import com.talk_space.modules.users.api.model.Speciality;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SpecialityDto {

    private String userName;

    private List<Speciality> specialities;
}
