package com.talk_space.model.dto;


import com.talk_space.model.enums.Education;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Component
public class EducationDto {

    private String userName;

    private Education education;
}
