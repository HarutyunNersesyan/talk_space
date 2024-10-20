package com.talk_space.service;


import com.talk_space.model.domain.Hobby;
import com.talk_space.repository.HobbyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HobbyService {

    private final HobbyRepository hobbyRepository;

    public Hobby save(Hobby hobby){
       return hobbyRepository.save(hobby);
    }

    public Optional<Hobby> getHobbyById(Long id){
        return hobbyRepository.findById(id);
    }

    public List<Hobby> getAll(){
        return hobbyRepository.findAll();
    }

    public Hobby update(Hobby hobby){
        return hobbyRepository.save(hobby);
    }

    public Optional<Hobby> getHobbyByName(String hobbyName){
        return hobbyRepository.findHobbyByName(hobbyName);

    }

    public void deleteHobbyById(Long id){
        hobbyRepository.deleteById(id);
    }

    public void deleteHobbyByName(String hobbyName){
        hobbyRepository.deleteHobbyByName(hobbyName);
    }

    public List<Hobby> saveHobbies(List<Hobby> hobbies){
        for (int i = 0; i < hobbies.size() - 1; i++) {
            hobbyRepository.save(hobbies.get(i));
        }
        return hobbies;
    }
}
