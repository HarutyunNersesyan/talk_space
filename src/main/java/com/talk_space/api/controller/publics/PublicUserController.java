package com.talk_space.api.controller.publics;

import com.talk_space.exceptions.CustomExceptions;
import com.talk_space.model.domain.*;
import com.talk_space.model.dto.*;
import com.talk_space.model.dto.getters.SocialNetworksGetterDto;
import com.talk_space.service.*;
import com.talk_space.validation.PasswordValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public/user")
public class PublicUserController {

    private final UserService userService;
    private final MailSenderService mailSenderService;
    private final LikeService likeService;
    private final ImageService imageService;
    private final HobbyService hobbyService;
    private final SocialNetworksService socialNetworksService;
    private final SpecialityService specialityService;


    @GetMapping("/get/userName/{email}")
    public ResponseEntity<String> getUserNameByEmail(@PathVariable String email) {
        Optional<User> userOptional = userService.findUserByEmail(email);

        if (userOptional.isPresent()) {
            return ResponseEntity.ok(userOptional.get().getUserName());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found with email: " + email);
        }
    }


    @PutMapping("/update/hobby")
    public ResponseEntity<String> updateHobbies(@RequestBody HobbyDto hobbyDto) {
        try {
            String result = hobbyService.addHobbyForUser(hobbyDto);
            return ResponseEntity.ok(result);
        } catch (CustomExceptions.UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }


    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id).orElseThrow(() -> new RuntimeException("User not found with id: " + id)));
    }

    @PostMapping("/signUp")
    public ResponseEntity<?> signUp(@Valid @RequestBody SignUp signUp, BindingResult result) {
        if (result.hasErrors()) {
            List<String> errors = result.getAllErrors()
                    .stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(errors);
        }
        if (!PasswordValidator.isValidPassword(signUp.getPassword())) {
            throw new IllegalArgumentException("Invalid password: password must be contain least one uppercase, one lowercase, one digit, and one special character");
        }

        User createdUser = new User(signUp);
        userService.signUp(createdUser);
        mailSenderService.handlePinRequest(signUp.getEmail(), false);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PutMapping("/update")
    public ResponseEntity<?> update(@Valid @RequestBody User user, BindingResult result) {
        if (result.hasErrors()) {
            List<String> errors = result.getAllErrors()
                    .stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(errors);
        }
        if (!PasswordValidator.isValidPassword(user.getPassword())) {
            throw new IllegalArgumentException("Invalid password: password must be contain least one uppercase, one lowercase, one digit, and one special character");
        }
        User updatedUser = userService.update(user);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    @PutMapping("/changePassword")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePassword changePassword, BindingResult result) {
        if (result.hasErrors()) {
            List<String> errors = result.getAllErrors()
                    .stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(errors);
        }
        if (!PasswordValidator.isValidPassword(changePassword.getNewPassword())) {
            throw new IllegalArgumentException("Invalid password: password must be contain least one uppercase, one lowercase, one digit, and one special character");
        }
        try {
            String responseMessage = userService.changePassword(changePassword.getEmail(), changePassword.getOldPassword(), changePassword.getNewPassword(), changePassword.getNewPasswordRepeat());
            return ResponseEntity.ok(responseMessage);
        } catch (CustomExceptions.NotVerifiedMailException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (CustomExceptions.UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (CustomExceptions.InvalidOldPasswordException | CustomExceptions.InvalidNewPasswordException |
                 CustomExceptions.PasswordMismatchException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/forgotPassword")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPassword forgotPassword) {
        try {
            String responseMessage = userService.forgotPassword(forgotPassword);
            return ResponseEntity.ok(responseMessage);
        } catch (CustomExceptions.NotVerifiedMailException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (CustomExceptions.InvalidPinExceptions | CustomExceptions.InvalidNewPasswordException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }


    @PutMapping("/verify")
    public ResponseEntity<String> verify(@RequestBody Verify verify) {
        try {
            String result = userService.verify(verify);
            return ResponseEntity.ok(result);
        } catch (CustomExceptions.AlreadyVerifiedEmail e) {
            return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(e.getMessage());
        } catch (CustomExceptions.InvalidPinExceptions e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }


    @DeleteMapping("/delete/account")
    public ResponseEntity<String> delete(@RequestBody DeleteAccount deleteAccount) {
        try {
            userService.delete(deleteAccount);
            return ResponseEntity.ok("Account deleted successfully.");
        } catch (CustomExceptions.UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (CustomExceptions.InvalidPassword e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }


    @PostMapping("/like")
    public ResponseEntity<?> like(@RequestBody Like like) {
        try {
            Like savedLike = likeService.saveLike(like);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedLike);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found: " + e.getMessage());
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Like already exists.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + e.getMessage());
        }
    }


    @PutMapping("/update/speciality")
    public ResponseEntity<String> updateSpecialities(@RequestBody SpecialityDto specialityDto) {
        try {
            String result = specialityService.addSpecialityForUser(specialityDto);
            return ResponseEntity.ok(result);
        } catch (CustomExceptions.UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }


    @PutMapping("/update/socialNetworks")
    public ResponseEntity<String> updateSocialNetworks(@RequestBody SocialNetworksDto socialNetworksDto) {
        try {
            String result = socialNetworksService.updateSocialNetworks(socialNetworksDto);
            return ResponseEntity.ok(result);
        } catch (CustomExceptions.UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (CustomExceptions.InvalidSocialNetworkException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }


    @PutMapping("/update/education")
    public ResponseEntity<String> updateEducation(@RequestBody EducationDto education) {
        return ResponseEntity.ok(userService.updateEducation(education));
    }


    @GetMapping("/searchByHobbies")
    public ResponseEntity<?> findUsersByHobbies(@RequestBody User user) {
        try {
            SearchUser searchUser = userService.findUsersByHobbies(user);
            return ResponseEntity.ok(searchUser);
        } catch (CustomExceptions.UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }

    @GetMapping("/searchBySpecialities")
    public ResponseEntity<?> findUsersBySpecialities(@RequestBody User user) {
        try {
            SearchUser searchUser = userService.findUsersBySpecialities(user);
            return ResponseEntity.ok(searchUser);
        } catch (CustomExceptions.UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }


    @PostMapping("/images/upload")
    public ResponseEntity<String> uploadImages(@RequestBody ImageDto imageDto) {
        imageService.updateImages(imageDto);
        return ResponseEntity.ok("Images uploaded successfully");
    }

    @GetMapping("/images/{userName}")
    public ResponseEntity<List<String>> getUserImages(@PathVariable String userName) {
        List<String> images = imageService.getUserImages(userName);
        return ResponseEntity.ok(images);
    }

    @DeleteMapping("/images/delete")
    public ResponseEntity<String> deleteImage(@RequestBody DeleteImageRequest request) {
        try {
            imageService.deleteImage(request.getUserName(), request.getFileName());
            return ResponseEntity.ok("Image deleted successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/socialNetworks/{userName}")
    public ResponseEntity<List<SocialNetworksGetterDto>> getAllSocialNetworks(@PathVariable String userName) {
        List<SocialNetworksGetterDto> socialNetworks = socialNetworksService.getAllSocialNetworks(userName);
        return new ResponseEntity<>(socialNetworks, HttpStatus.OK);
    }

    @GetMapping("/hobby")
    public List<Hobby> getAllHobbies() {
        return hobbyService.getAll();
    }

    @GetMapping("/hobby/{hobbyName}")
    public ResponseEntity<Hobby> getHobbyByName(@PathVariable String hobbyName) {
        return ResponseEntity.ok(hobbyService.getHobbyByName(hobbyName)
                .orElseThrow(() -> new RuntimeException("Hobby not found with hobby name: " + hobbyName)));
    }

    @GetMapping("/speciality")
    public List<Speciality> getAllSpecialities() {
        return specialityService.getAll();
    }

    @GetMapping("/speciality/{specialityName}")
    public ResponseEntity<Speciality> getSpecialityByName(@PathVariable String specialityName) {
        return ResponseEntity.ok(specialityService.getSpecialityByName(specialityName)
                .orElseThrow(() -> new RuntimeException("Speciality not found with hobby name: " + specialityName)));
    }

    @GetMapping("/get/specialities/{userName}")
    public ResponseEntity<List<Speciality>> getSpecialitesByUserName(@PathVariable String userName) {
        return userService.getUserByUserName(userName)
                .map(user -> ResponseEntity.ok(user.getSpecialities()))
                .orElseThrow(() -> new RuntimeException("User not found with user name: " + userName));
    }


    @GetMapping("/get/hobbies/{userName}")
    public ResponseEntity<List<Hobby>> getHobbiesByUserName(@PathVariable String userName) {
        return userService.getUserByUserName(userName)
                .map(user -> ResponseEntity.ok(user.getHobbies()))
                .orElseThrow(() -> new RuntimeException("User not found with user name: " + userName));
    }
}


