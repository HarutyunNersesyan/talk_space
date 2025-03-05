package com.talk_space.service;


import com.talk_space.exceptions.CustomExceptions;
import com.talk_space.model.domain.Hobby;
import com.talk_space.model.domain.User;
import com.talk_space.model.dto.HobbyDto;
import com.talk_space.repository.HobbyRepository;
import com.talk_space.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HobbyService {

    private final HobbyRepository hobbyRepository;

    private final UserRepository userRepository;

    public Hobby save(Hobby hobby) {
        return hobbyRepository.save(hobby);
    }

    public Optional<Hobby> getHobbyById(Long id) {
        return hobbyRepository.findById(id);
    }

    public List<Hobby> getAll() {
        return hobbyRepository.findAll();
    }

    public Hobby update(Hobby hobby) {
        return hobbyRepository.save(hobby);
    }

    public Optional<Hobby> getHobbyByName(String hobbyName) {
        return hobbyRepository.findHobbyByName(hobbyName);

    }

    public void deleteHobbyById(Long id) {
        hobbyRepository.deleteById(id);
    }

    public void deleteHobbyByName(String hobbyName) {
        hobbyRepository.deleteHobbyByName(hobbyName);
    }

    public List<Hobby> saveHobbies(List<Hobby> hobbies) {
        return hobbyRepository.saveAll(hobbies);
    }

    public String addHobbyForUser(HobbyDto hobbyDto) {
        if (hobbyDto.getHobbies().size() > 5) {
            throw new IllegalArgumentException("The number of hobbies cannot exceed 5");
        }

        Optional<User> optionalUser = userRepository.findUserByUserName(hobbyDto.getUserName());
        if (optionalUser.isEmpty()){
            throw new CustomExceptions.UserNotFoundException("User not found.");
        }
        User user = optionalUser.get();
        user.getHobbies().clear();

        for (int i = 0; i < hobbyDto.getHobbies().size(); i++) {
            user.getHobbies().add(hobbyRepository.findHobbyByName((hobbyDto.getHobbies().get(i)).getName()).get());
        };
        userRepository.save(user);

        return ("Hobby added successfully.");
    }

}