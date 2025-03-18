package com.talk_space.model.dto;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.talk_space.model.enums.Education;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class EducationDto {

    private String userName;

    private Education education;


    @JsonCreator
    public static EducationDto create(
            @JsonProperty("userName") String userName,
            @JsonProperty("education") String education) {
        return new EducationDto(userName, Education.valueOf(education));
    }
}
