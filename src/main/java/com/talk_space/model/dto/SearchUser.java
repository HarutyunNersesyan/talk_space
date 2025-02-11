package com.talk_space.model.dto;

import com.talk_space.model.domain.*;
import com.talk_space.model.enums.Education;
import com.talk_space.model.enums.Gender;
import com.talk_space.model.enums.Zodiac;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


@NoArgsConstructor
@AllArgsConstructor
@Data
@Component
public class SearchUser {


    private String firstName;

    private String lastName;

    private String userName;

    private Integer age;

    private Gender gender;

    private Zodiac zodiac;

    private List<Image> images;

    private List<String> hobbies = new LinkedList<>();

    private List<Speciality> specialities;

    private Education education;

    private List<SocialNetworks> socialNetworks;


    public SearchUser(Page<User> user) {
        for (int i = 0; i < user.getSize(); i++) {
            this.firstName = user.getContent().get(i).getFirstName();
            this.lastName = user.getContent().get(i).getLastName();
            this.userName = user.getContent().get(i).getUserName();
            this.age = LocalDate.now().getYear() - user.getContent().get(i).getBirthDate().getYear();
            this.gender = user.getContent().get(i).getGender();
            this.zodiac = user.getContent().get(i).getZodiacSign();
            this.images = user.getContent().get(i).getImages();
            for (int j = 0; j < user.getContent().get(i).getHobbies().size(); j++) {
                this.hobbies.add(user.getContent().get(i).getHobbies().get(j).getName());
            }
            this.specialities = user.getContent().get(i).getSpecialities();
            this.education = user.getContent().get(i).getEducation();
            this.socialNetworks = user.getContent().get(i).getSocialNetwork();
        }
    }
}
