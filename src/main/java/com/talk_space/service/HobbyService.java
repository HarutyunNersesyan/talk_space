package com.talk_space.service;


import com.talk_space.model.domain.Hobby;
import com.talk_space.model.domain.User;
import com.talk_space.model.dto.HobbyDto;
import com.talk_space.repository.HobbyRepository;
import com.talk_space.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
        hobbyRepository.saveAll(hobbies);
        return hobbies;
    }

    public ResponseEntity<String> addHobby(HobbyDto hobbyDto) {
        if (hobbyDto.getHobbies().size() > 5) {
            return ResponseEntity.badRequest().body("The number of hobbies cannot exceed 5");
        }

        Optional<User> optionalUser = userRepository.findUserByUserName(hobbyDto.getUserName());
        User user = optionalUser.get();
        List<Hobby> hobbies = new ArrayList<>();

        if (optionalUser.get().getHobbies().size() + hobbyDto.getHobbies().size() > 5) {
            return ResponseEntity.badRequest().body("The number of hobbies cannot exceed 5");
        }

        for (int i = 0; i < hobbyDto.getHobbies().size(); i++) {
            hobbies.add(hobbyRepository.findHobbyByName((hobbyDto.getHobbies().get(i)).getName()).get());
        }
        user.getHobbies().addAll(hobbies);
        userRepository.save(user);

        return ResponseEntity.ok("Hobby added successfully.");
    }
}
