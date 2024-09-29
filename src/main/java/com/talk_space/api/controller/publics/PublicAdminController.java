package com.talk_space.api.controller.publics;

import com.talk_space.model.domain.Hobby;
import com.talk_space.service.HobbyService;
import com.talk_space.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public/admin")
public class PublicAdminController {


    private final UserService userService;

    private final HobbyService hobbyService;


    @PostMapping("/hobby/save")
    public ResponseEntity<Hobby> saveHobby(@RequestBody Hobby hobby) {
        Hobby savedHobby = hobbyService.save(hobby);
        return new ResponseEntity<>(savedHobby, HttpStatus.CREATED);
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
                .orElseThrow(() -> new RuntimeException("Hobby not found with id: " + hobbyName)));
    }

    @DeleteMapping("/remove/id")
    private ResponseEntity<String> removeHobbyByName(@RequestParam String hobbyName) {
        hobbyService.deleteHobbyByName(hobbyName);
        return ResponseEntity.ok().body("Hobby by name " + hobbyName + " removed successfully");
    }

    @DeleteMapping("/remove/name")
    private ResponseEntity<String> removeHobbyById(@RequestParam Long id) {
        hobbyService.deleteHobbyById(id);
        return ResponseEntity.ok().body("Hobby by id " + id + " removed successfully");
    }

}
