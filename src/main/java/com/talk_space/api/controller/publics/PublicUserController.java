package com.talk_space.api.controller.publics;

import com.talk_space.model.domain.*;
import com.talk_space.model.dto.*;
import com.talk_space.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    private final HobbyService hobbyService;

    private final SocialNetworksService socialNetworksService;


    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id)));
    }

//    @GetMapping("/find")
//    public List<UserBasicInfo> getActiveUserBasicInfo() {
//        return userService.getActiveUserBasicInfo();
//    }

    @PostMapping("/signUp")
    public ResponseEntity<User> signUp(@Valid @RequestBody SignUp signUp) {
        User createdUser = new User(signUp);
        userService.save(createdUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
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
        return userService.verify(verify);
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

    @PostMapping("/update/image")
    public ResponseEntity<String> updateImage(@RequestBody ImageDto image) {
        return imageService.addImage(image);
    }


    @DeleteMapping("/image/delete/{id}")
    public ResponseEntity<Void> deleteImage(@PathVariable Long id) {
        imageService.deleteImage(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/update/hobby")
    public ResponseEntity<String> updateHobby(@RequestBody HobbyDto hobbyDto) {
        return hobbyService.addHobby(hobbyDto);
    }

    @PostMapping("/update/socialNetworks")
    public ResponseEntity<String> updateSocialNetworks(@RequestBody SocialNetworksDto sn) {
        return socialNetworksService.addSocialNetworks(sn);

    }

    @PutMapping("/update/phoneNumber")
    public ResponseEntity<String> updatePhoneNumber(@RequestBody PhoneNumberDto phoneNumberDto) {
        return userService.updatePhoneNumber(phoneNumberDto);
    }

    @PutMapping("/update/education")
    public ResponseEntity<String> updateEducation(@RequestBody EducationDto education) {
        return userService.updateEducation(education);
    }

//    @GetMapping("socialNetworks/{userName}")
//    public ResponseEntity<List<SocialNetworks>> getAllSocialNetworks(@PathVariable String userName) {
//        List<SocialNetworks> socialNetworks = socialNetworksService.getAllSocialNetworks(userName);
//        return new ResponseEntity<>(socialNetworks, HttpStatus.OK);
//    }
}


