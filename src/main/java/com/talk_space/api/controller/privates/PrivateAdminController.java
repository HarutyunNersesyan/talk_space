package com.talk_space.api.controller.privates;

import com.talk_space.model.domain.*;
import com.talk_space.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/private/admin")
public class PrivateAdminController {

    private final UserService userService;

    private final HobbyService hobbyService;


    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id)));
    }

    @GetMapping("/")
    public ResponseEntity<List<User>> getAll() {
        List<User> users = userService.getAll();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }


    @PostMapping("/hobby/save")
    public ResponseEntity<Hobby> saveHobby(@RequestBody Hobby hobby) {
        Hobby savedHobby = hobbyService.save(hobby);
        return new ResponseEntity<>(savedHobby, HttpStatus.CREATED);
    }

    @PostMapping("/hobby/saveAll")
    public ResponseEntity<List<Hobby>> saveHobbies(@RequestBody List<Hobby> hobbies) {
        List<Hobby> hobbyList = hobbyService.saveHobbies(hobbies);
        return new ResponseEntity<>(hobbyList, HttpStatus.CREATED);
    }


    @GetMapping("/hobby/")
    public List<Hobby> getAllHobbies() {
        return hobbyService.getAll();
    }

    @GetMapping("/hobby/{id}")
    public ResponseEntity<Hobby> getHobbyById(@PathVariable Long id) {
        return ResponseEntity.ok(hobbyService.getHobbyById(id)
                .orElseThrow(() -> new RuntimeException("Hobby not found with id: " + id)));
    }

    @GetMapping("/hobby/{hobbyName}")
    public ResponseEntity<Hobby> getHobbyByName(@PathVariable String hobbyName) {
        return ResponseEntity.ok(hobbyService.getHobbyByName(hobbyName)
                .orElseThrow(() -> new RuntimeException("Hobby not found with hobby name: " + hobbyName)));
    }

    @DeleteMapping("/hobby/remove/hobbyName")
    private ResponseEntity<String> removeHobbyByName(@RequestParam String hobbyName) {
        hobbyService.deleteHobbyByName(hobbyName);
        return ResponseEntity.ok().body("Hobby by name " + hobbyName + " removed successfully");
    }

    @DeleteMapping("/hobby/remove/id")
    private ResponseEntity<String> removeHobbyById(@RequestParam Long id) {
        hobbyService.deleteHobbyById(id);
        return ResponseEntity.ok().body("Hobby by id " + id + " removed successfully");
    }


}



