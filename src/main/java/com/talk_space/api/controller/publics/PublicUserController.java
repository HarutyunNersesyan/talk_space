package com.talk_space.api.controller.publics;

import com.talk_space.exceptions.CustomExceptions;
import com.talk_space.model.domain.*;
import com.talk_space.model.dto.*;
import com.talk_space.model.dto.getters.SocialNetworksGetterDto;
import com.talk_space.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Stack;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public/user")
public class PublicUserController {

    private final UserService userService;


    private final LikeService likeService;

    private final ChatMessageService chatService;

    private final ImageService imageService;

    private final HobbyService hobbyService;

    private final SocialNetworksService socialNetworksService;

    private final SpecialityService specialityService;

    private Stack<SearchUser> searchUsersWithSpeciality = new Stack<>();

    private Stack<SearchUser> searchUsersWithHobbies = new Stack<>();


    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id).orElseThrow(() -> new RuntimeException("User not found with id: " + id)));
    }

    @PostMapping("/signUp")
    public ResponseEntity<User> signUp(@Valid @RequestBody SignUp signUp) {
        User createdUser = new User(signUp);
        userService.signUp(createdUser);
//        mailSenderService.handlePinRequest(signUp.getEmail(), false);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PutMapping("/update")
    public ResponseEntity<User> update(@Valid @RequestBody User user) {
        User updatedUser = userService.update(user);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    @PutMapping("/changePassword")
    public ResponseEntity<String> changePassword(@RequestBody ChangePassword changePassword) {
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
    public ResponseEntity<String> updateSocialNetworks(@RequestBody SocialNetworksDto sn) {
        try {
            String result = socialNetworksService.addSocialNetworks(sn);
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

    @GetMapping("/searchByHobbies/{offset}/{pageSize}")
    public ResponseEntity<?> findUsersByHobbies(@RequestBody User user, @PathVariable int offset,
                                                @PathVariable int pageSize) {
        try {
            if (!searchUsersWithHobbies.isEmpty()) {
                return ResponseEntity.ok(searchUsersWithHobbies.pop());
            }
            searchUsersWithHobbies = userService.findUsersByHobbies(user, offset, pageSize);
            return ResponseEntity.ok(searchUsersWithHobbies.pop());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }


    @GetMapping("/searchBySpecialities/{offset}/{pageSize}")
    public ResponseEntity<?> findUsersBySpecialities(@RequestBody User user, @PathVariable int offset,
                                                     @PathVariable int pageSize) {
        try {
            if (!searchUsersWithSpeciality.isEmpty()) {
                return ResponseEntity.ok(searchUsersWithSpeciality.pop());
            }
            searchUsersWithSpeciality = userService.findUsersBySpecialities(user, offset, pageSize, searchUsersWithSpeciality);
            return ResponseEntity.ok(searchUsersWithSpeciality.pop());
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
}


