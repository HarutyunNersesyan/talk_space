package com.talk_space.api.controller.privates;

import com.talk_space.exceptions.CustomExceptions;
import com.talk_space.model.domain.*;
import com.talk_space.model.dto.APIResponse;
import com.talk_space.model.dto.BlockAccount;
import com.talk_space.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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

    private final SpecialityService specialityService;

    private final ReviewService reviewService;

    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id)));
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<User>> getAll() {
        List<User> users = userService.getAll();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @GetMapping("/pagination/{offset}/{pageSize}/{field}")
    public APIResponse<Page<User>> getUsersWithPaginationAndSorting(@PathVariable int offset,
                                                                    @PathVariable int pageSize,
                                                                    @PathVariable String field) {
        Page<User> users = userService.getUsersWithPaginationAndSorting(offset, pageSize, field);
        return new APIResponse<>(users.getSize(), users);
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

    @GetMapping("/hobby")
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
    public ResponseEntity<String> removeHobbyByName(@RequestParam String hobbyName) {
        hobbyService.deleteHobbyByName(hobbyName);
        return ResponseEntity.ok()
                .body("Hobby by name " + hobbyName + " removed successfully");
    }

    @DeleteMapping("/hobby/remove/id")
    public ResponseEntity<String> removeHobbyById(@RequestParam Long id) {
        hobbyService.deleteHobbyById(id);
        return ResponseEntity.ok()
                .body("Hobby by id " + id + " removed successfully");
    }

    @PutMapping("/hobby/update")
    public ResponseEntity<Hobby> updateHobby(@RequestParam Hobby hobby) {
        Hobby updateHobby = hobbyService.update(hobby);
        return new ResponseEntity<>(updateHobby, HttpStatus.OK);
    }

    @PostMapping("/speciality/save")
    public ResponseEntity<Speciality> saveSpeciality(@RequestBody Speciality speciality) {
        Speciality savedSpeciality = specialityService.save(speciality);
        return new ResponseEntity<>(savedSpeciality, HttpStatus.CREATED);
    }

    @PostMapping("/speciality/saveAll")
    public ResponseEntity<List<Speciality>> saveSpecialities(@RequestBody List<Speciality> specialities) {
        List<Speciality> specialityList = specialityService.saveSpecialities(specialities);
        return new ResponseEntity<>(specialityList, HttpStatus.CREATED);
    }


    @GetMapping("/speciality")
    public List<Speciality> getAllSpecialities() {
        return specialityService.getAll();
    }

    @GetMapping("/speciality/{id}")
    public ResponseEntity<Speciality> getSpecialityById(@PathVariable Long id) {
        return ResponseEntity.ok(specialityService.getSpecialityById(id)
                .orElseThrow(() -> new RuntimeException("Hobby not found with id: " + id)));
    }


    @GetMapping("/speciality/{specialityName}")
    public ResponseEntity<Speciality> getSpecialityByName(@PathVariable String specialityName) {
        return ResponseEntity.ok(specialityService.getSpecialityByName(specialityName)
                .orElseThrow(() -> new RuntimeException("Speciality not found with hobby name: " + specialityName)));
    }


    @DeleteMapping("/speciality/remove/specialityName")
    public ResponseEntity<String> removeSpecialityByName(@RequestParam String specialityName) {
        specialityService.deleteSpecialityByName(specialityName);
        return ResponseEntity.ok()
                .body("Speciality by name " + specialityName + " removed successfully");
    }

    @DeleteMapping("/speciality/remove/id")
    public ResponseEntity<String> removeSpecialityById(@RequestParam Long id) {
        specialityService.deleteSpecialityById(id);
        return ResponseEntity.ok()
                .body("Speciality by id " + id + " removed successfully");
    }


    @PutMapping("/block")
    public ResponseEntity<String> blockUser(@RequestBody BlockAccount blockAccount) {
        try {
            String result = userService.blockUser(blockAccount);
            return ResponseEntity.ok().body("User has been successfully blocked. " +
                    "\n" + "Cause: " + result);
        } catch (CustomExceptions.UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PutMapping("/unBlock")
    public ResponseEntity<String> unblockUser(@RequestParam String username) {
        try {
            String result = userService.unblockUser(username);
            return ResponseEntity.ok().body(result);
        } catch (CustomExceptions.UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
    @GetMapping("/review")
    public List<Review> getAllReviews(){
        return reviewService.getAll();
    }
}



