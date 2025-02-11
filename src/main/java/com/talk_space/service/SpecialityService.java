package com.talk_space.service;


import com.talk_space.model.domain.Hobby;
import com.talk_space.model.domain.Speciality;
import com.talk_space.model.domain.User;
import com.talk_space.model.dto.SpecialityDto;
import com.talk_space.repository.SpecialityRepository;
import com.talk_space.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SpecialityService {


    private final SpecialityRepository specialityRepository;

    private final UserRepository userRepository;


    public Speciality save(Speciality speciality) {
        return specialityRepository.save(speciality);
    }

    public Optional<Speciality> getSpecialityById(Long id) {

        return specialityRepository.findById(id);
    }

    public List<Speciality> getAll() {
        return specialityRepository.findAll();
    }

    public Optional<Speciality> getSpecialityByName(String specialityName){
        return specialityRepository.findSpecialityByName(specialityName);
    }
    public Speciality update(Speciality speciality) {
        return specialityRepository.save(speciality);
    }

    public void deleteSpecialityById(Long id) {
        specialityRepository.deleteById(id);
    }

    public void deleteSpecialityByName(String name) {
        specialityRepository.deleteSpecialityByName(name);
    }

    public List<Speciality> saveSpecialities(List<Speciality> specialities) {
        return specialityRepository.saveAll(specialities);
    }

    public String addSpecialityForUser(SpecialityDto specialityDto) {
        if (specialityDto.getSpecialities().size() > 5) {
            throw new IllegalArgumentException("The number of specialities cannot exceed 5");
        }

        Optional<User> optionalUser = userRepository.findUserByUserName(specialityDto.getUserName());
        User user = optionalUser.get();
        List<Speciality> specialities = new ArrayList<>();

        if (optionalUser.get().getSpecialities().size() + specialityDto.getSpecialities().size() > 5) {
            throw new IllegalArgumentException("The number of specialities cannot exceed 5");
        }

        for (int i = 0; i < specialityDto.getSpecialities().size(); i++) {
            specialities.add(specialityRepository.findSpecialityByName((specialityDto.getSpecialities().get(i)).getName()).get());
        }
        user.getSpecialities().addAll(specialities);
        userRepository.save(user);

        return ("Specialities added successfully.");
    }


}
