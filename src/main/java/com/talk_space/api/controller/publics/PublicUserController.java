package com.talk_space.api.controller.publics;

import com.talk_space.exceptions.CustomExceptions;
import com.talk_space.model.domain.*;
import com.talk_space.model.dto.*;
import com.talk_space.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

    private final SpecialityService specialityService;


    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id).orElseThrow(() -> new RuntimeException("User not found with id: " + id)));
    }

    @PostMapping("/signUp")
    public ResponseEntity<User> signUp(@Valid @RequestBody SignUp signUp) {
        User createdUser = new User(signUp);
        userService.signUp(createdUser);
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
    public ResponseEntity<Like> createLike(@RequestBody Like like) {
        Like savedLike = likeService.saveLike(like);
        return new ResponseEntity<>(savedLike, HttpStatus.CREATED);
    }

//    @GetMapping("/chats/{id}")
//    public List<Chat> getAllUserChats(@PathVariable Long id) {
//        Optional<User> user = userService.getUserById(id);
//        return chatService.getAllUserChats(user.get());
//    }

//    @PutMapping("/update/image")
//    public ResponseEntity<String> updateImage(@RequestBody ImageDto imageDto) {
//        try {
//            imageService.addImage(imageDto);
//            return ResponseEntity.ok("Images updated successfully");
//        } catch (CustomExceptions.UserNotFoundException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
//        } catch (CustomExceptions.ImageLimitExceededException e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
//        }
//    }

    @DeleteMapping("/image/delete/{id}")
    public ResponseEntity<Void> deleteImage(@PathVariable Long id) {
        imageService.deleteImage(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/update/hobby")
    public ResponseEntity<String> updateHobby(@RequestBody HobbyDto hobbyDto) {
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
    public ResponseEntity<String> updateSpeciality(@RequestBody SpecialityDto specialityDto) {
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

    @GetMapping("/search/{offset}/{pageSize}")
    public APIResponse<Page<User>> findUsers(@RequestBody User user, @RequestParam(defaultValue = "0", required = false) int offset,
                                             @RequestParam(defaultValue = "2", required = false) int pageSize) {
        Page<User> users = userService.findUsers(user, offset, pageSize);
        return new APIResponse<>(users.getSize(), users);
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(@RequestBody ImageDto imageDto) {
        try {
            imageService.saveImages(imageDto);  // Pass ImageDto to service
            return ResponseEntity.ok("Images uploaded successfully");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error uploading images");
        }
    }

    // Get images for a user by userName
    @GetMapping("/user/{userName}")
    public ResponseEntity<List<byte[]>> getUserImages(@PathVariable String userName) {
        return ResponseEntity.ok(imageService.getImagesByUserName(userName));
    }


//    @GetMapping("socialNetworks/{userName}")
//    public ResponseEntity<List<SocialNetworks>> getAllSocialNetworks(@PathVariable String userName) {
//        List<SocialNetworks> socialNetworks = socialNetworksService.getAllSocialNetworks(userName);
//        return new ResponseEntity<>(socialNetworks, HttpStatus.OK);
//    }
}


