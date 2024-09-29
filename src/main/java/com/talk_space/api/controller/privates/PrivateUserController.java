package com.talk_space.api.controller.privates;

import com.talk_space.model.domain.User;
import com.talk_space.model.dto.ChangePassword;
import com.talk_space.model.dto.ForgotPassword;
import com.talk_space.model.dto.Verify;
import com.talk_space.model.enums.Role;
import com.talk_space.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/private/user")
public class PrivateUserController {

    private final UserService userService;

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

    @PostMapping("/save")
    public ResponseEntity<User> save(@Valid @RequestBody User user) {
        User savedUser = userService.save(user);
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }

    @PutMapping("/update")
    public ResponseEntity<User> update(@Valid @RequestBody User user) {
        User updatedUser = userService.update(user);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

//    @PutMapping("/changePassword")
//    public ResponseEntity<?> changePassword(@RequestBody ChangePassword changePassword) {
//        return   userService.changePassword(changePassword.getEmail(), changePassword.getOldPassword(),
//                changePassword.getNewPassword(), changePassword.getNewPasswordRepeat());
//    }
//
//    @PutMapping("/forgotPassword")
//    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPassword forgotPassword) {
//        return userService.forgotPassword(forgotPassword);
//    }
//
//    @PutMapping("/verify")
//    public ResponseEntity<String> verify(@RequestBody Verify verify) {
//        return userService.findUserByEmail(verify.getEmail())
//                .filter(user -> user.getPin().equals(verify.getPin()))
//                .map(user -> {
//                    user.setIsActive(true);
//                    userService.save(user);
//                    return ResponseEntity.ok("Verifying");
//                })
//                .orElse(ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid pin"));
//    }
//
//
//    @PostMapping("/save")
//    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
//        user.setCreatedDate(LocalDate.now());
//        user.setRole(Role.USER);
//        user.setPassword(userService.hashPassword(user.getPassword()));
//        User savedUser = userService.save(user);
//        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
//    }
}



