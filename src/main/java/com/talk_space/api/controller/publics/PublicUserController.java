package com.talk_space.api.controller.publics;

import com.talk_space.model.domain.Chat;
import com.talk_space.model.domain.Image;
import com.talk_space.model.domain.Like;
import com.talk_space.model.domain.User;
import com.talk_space.model.dto.*;
import com.talk_space.model.enums.Role;
import com.talk_space.service.ChatService;
import com.talk_space.service.ImageService;
import com.talk_space.service.LikeService;
import com.talk_space.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public/user")
public class PublicUserController {

    private final UserService userService;
    private final LikeService likeService;

    private final ChatService chatService;

    private final ImageService imageService;


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

    @PutMapping("/update")
    public ResponseEntity<User> update(@Valid @RequestBody User user) {
        User updatedUser = userService.update(user);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    @PutMapping("/changePassword")
    public ResponseEntity<?> changePassword(@RequestBody ChangePassword changePassword) {
        return userService.changePassword(changePassword.getEmail(), changePassword.getOldPassword(),
                changePassword.getNewPassword(), changePassword.getNewPasswordRepeat());
    }

    @PutMapping("/forgotPassword")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPassword forgotPassword) {
        return userService.forgotPassword(forgotPassword);
    }

    @PutMapping("/verify")
    public ResponseEntity<String> verify(@RequestBody Verify verify) {
        return userService.findUserByEmail(verify.getEmail())
                .filter(user -> user.getPin().equals(verify.getPin()))
                .map(user -> {
                    user.setIsActive(true);
                    userService.save(user);
                    return ResponseEntity.ok("Verifying");
                })
                .orElse(ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid pin"));
    }


    @PostMapping("/signUp")
    public ResponseEntity<User> signUp(@Valid @RequestBody SignUp signUp) {
        User createdUser = new User(signUp);
        userService.save(createdUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }


    @DeleteMapping("/delete/account")
    public ResponseEntity<String> delete(@RequestBody DeleteAccount deleteAccount) {
        userService.delete(deleteAccount);
        return new ResponseEntity<>("Account deleted successfully", HttpStatus.OK);
    }


    @PostMapping("/like")
    public ResponseEntity<Like> createLike(@RequestBody Like like) {
        Like savedLike = likeService.saveLike(like);
        return new ResponseEntity<>(savedLike, HttpStatus.CREATED);
    }

    @GetMapping("/chats/{id}")
    public List<Chat> getAllUserChats(@PathVariable Long id) {
        Optional<User> user = userService.getUserById(id);
        return chatService.getAllUserChats(user.get());
    }

    @PostMapping("/add/image")
    public ResponseEntity<Image> addImage(@RequestBody Image image) {
        Image savedImage = imageService.addImage(image);
        return new ResponseEntity<>(savedImage, HttpStatus.CREATED);
    }


    @DeleteMapping("/image/delete/{id}")
    public ResponseEntity<Void> deleteImage(@PathVariable Long id) {
        imageService.deleteImage(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }



}


