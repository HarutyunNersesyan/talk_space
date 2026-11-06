package com.talk_space.modules.users.api.dto;
import com.talk_space.modules.users.api.model.User;

import com.talk_space.modules.users.api.model.Gender;
import com.talk_space.modules.users.api.model.Zodiac;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.*;


@NoArgsConstructor
@AllArgsConstructor
@Data
public class SearchUser {


    private com.talk_space.modules.users.api.dto.MatchEvaluation matching;

    private String firstName;

    private String lastName;

    private String userName;

    private Integer age;

    private Gender gender;

    private Zodiac zodiac;

    private String image;

    private String about;

    private List<String> hobbies = new ArrayList<>();

    private List<String> specialities = new ArrayList<>();


    private List<String> socialNetworks = new ArrayList<>();


//    public  SearchUser(Page<User> user) {
//        for (int i = 0; i < user.getSize(); i++) {
//            this.firstName = user.getContent().get(i).getFirstName();
//            this.lastName = user.getContent().get(i).getLastName();
//            this.userName = user.getContent().get(i).getUserName();
//            this.age = LocalDate.now().getYear() - user.getContent().get(i).getBirthDate().getYear();
//            this.gender = user.getContent().get(i).getGender();
//            this.zodiac = user.getContent().get(i).getZodiacSign();
//            this.images = user.getContent().get(i).getImages();
//            for (int j = 0; j < user.getContent().get(i).getHobbies().size(); j++) {
//                this.hobbies.add(user.getContent().get(i).getHobbies().get(j).getName());
//            }
//            this.specialities = user.getContent().get(i).getSpecialities();
//            this.education = user.getContent().get(i).getEducation();
//            this.socialNetworks = user.getContent().get(i).getSocialNetwork();
//        }
//    }

    public SearchUser(User user) {
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.userName = user.getUserName();
        this.age = user.getBirthDate()==null ? null : java.time.Period.between(user.getBirthDate(), LocalDate.now()).getYears();
        this.gender = user.getGender();
        this.zodiac = user.getZodiacSign();
        this.about = user.getAboutMe();
        if (user.getImage() != null) {
            this.image = user.getImage().getFilePath();
        }

        if (user.getHobbies() != null && !user.getHobbies().isEmpty()) {
            for (int i = 0; i < user.getHobbies().size(); i++) {
                this.hobbies.add(user.getHobbies().get(i).getName());
            }
        }

        if (user.getSpecialities() != null && !user.getSpecialities().isEmpty()) {
            for (int i = 0; i < user.getSpecialities().size(); i++) {
                this.specialities.add(user.getSpecialities().get(i).getName());
            }
        }

        if (user.getSocialNetworks() != null && !user.getSocialNetworks().isEmpty()) {
            for (int i = 0; i < user.getSocialNetworks().size(); i++) {
                this.socialNetworks.add(user.getSocialNetworks().get(i).getUrl());
            }
        }
    }
}
