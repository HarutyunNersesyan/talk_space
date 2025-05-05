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
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
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

    private final ReviewService reviewService;


    @GetMapping("/get/userName/{email}")
    public ResponseEntity<String> getUserNameByEmail(@PathVariable String email) {
        Optional<User> userOptional = userService.findUserByEmail(email);

        if (userOptional.isPresent()) {
            return ResponseEntity.ok(userOptional.get().getUserName());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found with email: " + email);
        }
    }

    @GetMapping("/profile/{email}")
    public ResponseEntity<SearchUser> getProfile(@PathVariable String email) {
        Optional<User> userOptional = userService.findUserByEmail(email);
        SearchUser searchUser = new SearchUser(userOptional.get());
        return ResponseEntity.ok(searchUser);
    }

    @GetMapping("/edit/{email}")
    public ResponseEntity<EditUser> getInfo(@PathVariable String email) {
        Optional<User> userOptional = userService.findUserByEmail(email);
        EditUser editUser = new EditUser(userOptional.get());
        return ResponseEntity.ok(editUser);
    }

    @GetMapping("/get/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        Optional<User> userOptional = userService.findUserByEmail(email);
        return userOptional
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
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
    public ResponseEntity<?> signUp(@javax.validation.Valid @RequestBody SignUp signUp, BindingResult result) {

        if (result.hasErrors()) {
            List<String> errors = result.getAllErrors()
                    .stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(errors);
        }

        if (userService.findUserByUserName(signUp.getUserName()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("This username is already taken. Please use a different username.");
        }

        if (userService.findUserByEmail(signUp.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("This email address is already registered. Please use a different email.");
        }

        if (!PasswordValidator.isValidPassword(signUp.getPassword())) {
            return ResponseEntity.badRequest()
                    .body("Invalid password: password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character.");
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

    @PutMapping("/editUser")
    public ResponseEntity<User> editUser(@RequestParam String email,
                                         @RequestBody @Valid EditUser editUser) {
        User updatedUser = userService.updateUserByEmail(email, editUser);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/changePassword")
    public ResponseEntity<?> changePassword(@RequestParam String email,
                                            @Valid @RequestBody ChangePassword changePassword, BindingResult result) {
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
            String responseMessage = userService.changePassword(email, changePassword.getOldPassword(), changePassword.getNewPassword(), changePassword.getNewPasswordRepeat());
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

    @DeleteMapping("/delete/verify/{email}")
    public ResponseEntity<String> cancelVerify(@PathVariable String email) {
        try {
            userService.deleteVerify(email);
            return ResponseEntity.ok("Account deleted successfully.");
        } catch (CustomExceptions.UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }


    @PostMapping("/like")
    public ResponseEntity<?> like(@RequestParam String liker,
                                  @RequestParam String liked) {
        try {
            Like savedLike = likeService.saveLike(liker, liked);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    Map.of(
                            "status", "success",
                            "message", "Like saved successfully",
                            "data", savedLike
                    )
            );
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of(
                            "status", "error",
                            "message", "User not found: " + e.getMessage()
                    )
            );
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    Map.of(
                            "status", "error",
                            "message", "Like already exists"
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            Map.of(
                                    "status", "error",
                                    "message", "An unexpected error occurred: " + e.getMessage()
                            )
                    );
        }
    }

    @GetMapping("/like/get")
    public Boolean getLike(
            @RequestParam String liker,
            @RequestParam String liked
    ) {
        return likeService.getLike(liker, liked);
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


    @PutMapping("/update/aboutMe")
    public ResponseEntity<String> updateAboutMe(@RequestBody AboutMe aboutMe) {
        try {
            String result = userService.updateAboutMe(aboutMe);
            return ResponseEntity.ok(result);
        } catch (CustomExceptions.UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/aboutMe/{userName}")
    public ResponseEntity<String> getAboutMe(@PathVariable String userName) {
        try {
            String result = userService.getUserByUserName(userName).get().getAboutMe();
            return ResponseEntity.ok(result);
        } catch (CustomExceptions.UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/searchByHobbies/{userName}")
    public ResponseEntity<?> findUsersByHobbies(@PathVariable String userName) {
        try {
            SearchUser searchUser = userService.findUsersByHobbies(userName);
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

    @GetMapping("/searchBySpecialities/{userName}")
    public ResponseEntity<?> findUsersBySpecialities(@PathVariable String userName) {
        try {
            SearchUser searchUser = userService.findUsersBySpecialities(userName);
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

    @GetMapping("/username/{userName}")
    public ResponseEntity<User> getUserByUserName(@PathVariable String userName) {
        Optional<User> userOptional = userService.getUserByUserName(userName);
        return userOptional
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("image/upload")
    public ResponseEntity<String> uploadProfilePicture(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userName") String userName) {
        try {
            String filePath = imageService.uploadProfilePicture(file, userName);
            return ResponseEntity.ok("Profile picture uploaded successfully: " + filePath);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload profile picture: " + e.getMessage());
        }
    }

    @GetMapping("/image/{userName}")
    public ResponseEntity<Resource> getUserImage(@PathVariable String userName) {
        try {
            String fileName = imageService.getUserImage(userName);
            Path filePath = Paths.get("C:/Users/HARUT_037/Desktop/talk_space/images/" + fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                String contentType = fileName.endsWith(".png") ?
                        MediaType.IMAGE_PNG_VALUE : MediaType.IMAGE_JPEG_VALUE;

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/image/delete/{userName}")
    public ResponseEntity<String> deleteUserImage(@PathVariable String userName) {
        try {
            imageService.deleteUserImage(userName);
            return ResponseEntity.ok("User image deleted successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete user image: " + e.getMessage());
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
    public ResponseEntity<List<Speciality>> getSpecialitiesByUserName(@PathVariable String userName) {
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

    @PostMapping("/review/add")
    public Review addReview(@RequestParam String userName, @RequestParam String message){
       return reviewService.addReview(message,userName);
    }
}


