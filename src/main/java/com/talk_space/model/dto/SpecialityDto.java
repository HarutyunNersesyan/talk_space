package com.talk_space.model.dto;


import com.talk_space.model.domain.Speciality;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Component
public class SpecialityDto {

    private String userName;

    private List<Speciality> specialities;
}
