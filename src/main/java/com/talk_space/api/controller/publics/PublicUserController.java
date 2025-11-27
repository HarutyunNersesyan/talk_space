package com.talk_space.api.controller.publics;

import com.talk_space.exceptions.CustomExceptions;
import com.talk_space.model.domain.*;
import com.talk_space.model.dto.*;
import com.talk_space.model.dto.getters.SocialNetworksGetterDto;
import com.talk_space.service.*;
import com.talk_space.util.LoggingUtil;
import com.talk_space.validation.PasswordValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public/user")
public class PublicUserController {
    private static final Logger logger = LoggerFactory.getLogger(PublicUserController.class);

    private final UserService userService;
    private final MailSenderService mailSenderService;
    private final LikeService likeService;
    private final ImageService imageService;
    private final HobbyService hobbyService;
    private final SocialNetworksService socialNetworksService;
    private final SpecialityService specialityService;

    private final FeedBacksService feedBacksService;


    @GetMapping("/get/userName/{email}")
    public ResponseEntity<String> getUserNameByEmail(@PathVariable String email) {
        logger.debug("Getting username by email: {}", email);
        Optional<User> userOptional = userService.findUserByEmail(email);

        if (userOptional.isPresent()) {
            logger.debug("Found username: {} for email: {}", userOptional.get().getUserName(), email);
            return ResponseEntity.ok(userOptional.get().getUserName());
        } else {
            logger.warn("User not found for email: {}", email);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found with email: " + email);
        }
    }

    @GetMapping("/profile/{email}")
    public ResponseEntity<SearchUser> getProfile(@PathVariable String email) {
        logger.debug("Getting profile for email: {}", email);
        Optional<User> userOptional = userService.findUserByEmail(email);
        SearchUser searchUser = new SearchUser(userOptional.get());
        return ResponseEntity.ok(searchUser);
    }

//    @GetMapping("/profile/{userName}")
//    public ResponseEntity<SearchUser> viewProfile(@PathVariable String userName){
//        logger.debug("Viewing profile for username: {}", userName);
//        Optional<User> userOptional = userService.findUserByUserName(userName);
//        SearchUser searchUser = new SearchUser(userOptional.get());
//        return ResponseEntity.ok(searchUser);
//    }

    @GetMapping("/edit/{email}")
    public ResponseEntity<EditUser> getInfo(@PathVariable String email) {
        logger.debug("Getting edit info for email: {}", email);
        Optional<User> userOptional = userService.findUserByEmail(email);
        EditUser editUser = new EditUser(userOptional.get());
        return ResponseEntity.ok(editUser);
    }

    @GetMapping("/get/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        logger.debug("Getting user by email: {}", email);
        Optional<User> userOptional = userService.findUserByEmail(email);
        return userOptional
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @PutMapping("/update/hobby")
    public ResponseEntity<String> updateHobbies(@RequestBody HobbyDto hobbyDto) {
        logger.debug("Updating hobbies for user: {}", hobbyDto.getUserName());
        try {
            String result = hobbyService.addHobbyForUser(hobbyDto);
            logger.info("Hobbies updated successfully for user: {}", hobbyDto.getUserName());
            return ResponseEntity.ok(result);
        } catch (CustomExceptions.UserNotFoundException e) {
            logger.warn("User not found for hobby update: {}", hobbyDto.getUserName());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid hobby data for user: {}", hobbyDto.getUserName());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error updating hobbies for user: {}", hobbyDto.getUserName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }


    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable Long id) {
        logger.debug("Getting user by ID: {}", id);
        return ResponseEntity.ok(userService.getUserById(id).orElseThrow(() -> {
            logger.warn("User not found with ID: {}", id);
            return new RuntimeException("User not found with id: " + id);
        }));
    }

    @PostMapping("/signUp")
    public ResponseEntity<?> signUp(@javax.validation.Valid @RequestBody SignUp signUp, BindingResult result) {
        logger.info("Received signup request for username: {}", signUp.getUserName());

        if (result.hasErrors()) {
            List<String> errors = result.getAllErrors()
                    .stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.toList());
            logger.warn("Signup validation failed for {}: {}", signUp.getUserName(), errors);
            return ResponseEntity.badRequest().body(errors);
        }

        if (userService.findUserByUserName(signUp.getUserName()).isPresent()) {
            logger.warn("Signup failed - username already taken: {}", signUp.getUserName());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("This username is already taken. Please use a different username.");
        }

        if (userService.findUserByEmail(signUp.getEmail()).isPresent()) {
            logger.warn("Signup failed - email already registered: {}", signUp.getEmail());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("This email address is already registered. Please use a different email.");
        }

        if (!PasswordValidator.isValidPassword(signUp.getPassword())) {
            logger.warn("Signup failed - invalid password for: {}", signUp.getUserName());
            return ResponseEntity.badRequest()
                    .body("Invalid password: password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character.");
        }

        User createdUser = new User(signUp);
        userService.signUp(createdUser);

        mailSenderService.handlePinRequest(signUp.getEmail(), false);

        logger.info("Signup completed successfully for: {}", signUp.getUserName());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PutMapping("/update")
    public ResponseEntity<?> update(@Valid @RequestBody User user, BindingResult result) {
        logger.debug("Updating user: {}", user.getUserName());
        if (result.hasErrors()) {
            List<String> errors = result.getAllErrors()
                    .stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.toList());
            logger.warn("User update validation failed for {}: {}", user.getUserName(), errors);
            return ResponseEntity.badRequest().body(errors);
        }
        if (!PasswordValidator.isValidPassword(user.getPassword())) {
            logger.warn("Invalid password during update for: {}", user.getUserName());
            throw new IllegalArgumentException("Invalid password: password must be contain least one uppercase, one lowercase, one digit, and one special character");
        }
        User updatedUser = userService.update(user);
        logger.info("User updated successfully: {}", user.getUserName());
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    @PutMapping("/editUser")
    public ResponseEntity<User> editUser(@RequestParam String email,
                                         @RequestBody @Valid EditUser editUser) {
        logger.debug("Editing user with email: {}", email);
        User updatedUser = userService.updateUserByEmail(email, editUser);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/changePassword")
    public ResponseEntity<?> changePassword(@RequestParam String email,
                                            @Valid @RequestBody ChangePassword changePassword, BindingResult result) {
        logger.debug("Password change request for: {}", email);
        if (result.hasErrors()) {
            List<String> errors = result.getAllErrors()
                    .stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.toList());
            logger.warn("Password change validation failed for {}: {}", email, errors);
            return ResponseEntity.badRequest().body(errors);
        }
        if (!PasswordValidator.isValidPassword(changePassword.getNewPassword())) {
            logger.warn("Invalid new password for: {}", email);
            throw new IllegalArgumentException("Invalid password: password must be contain least one uppercase, one lowercase, one digit, and one special character");
        }
        try {
            String responseMessage = userService.changePassword(email, changePassword.getOldPassword(), changePassword.getNewPassword(), changePassword.getNewPasswordRepeat());
            logger.info("Password changed successfully for: {}", email);
            return ResponseEntity.ok(responseMessage);
        } catch (CustomExceptions.NotVerifiedMailException e) {
            logger.warn("Password change failed - email not verified: {}", email);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (CustomExceptions.UserNotFoundException e) {
            logger.warn("Password change failed - user not found: {}", email);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (CustomExceptions.InvalidOldPasswordException | CustomExceptions.InvalidNewPasswordException |
                 CustomExceptions.PasswordMismatchException e) {
            logger.warn("Password change failed - validation error for {}: {}", email, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Password change failed - internal error for: {}", email, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/forgotPassword")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPassword forgotPassword) {
        logger.debug("Forgot password request for: {}", forgotPassword.getEmail());
        try {
            String responseMessage = userService.forgotPassword(forgotPassword);
            return ResponseEntity.ok(responseMessage);
        } catch (CustomExceptions.NotVerifiedMailException e) {
            logger.warn("Forgot password failed - email not verified: {}", forgotPassword.getEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (CustomExceptions.InvalidPinExceptions | CustomExceptions.InvalidNewPasswordException e) {
            logger.warn("Forgot password failed - validation error for {}: {}", forgotPassword.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Forgot password failed - internal error for: {}", forgotPassword.getEmail(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }


    @PutMapping("/verify")
    public ResponseEntity<String> verify(@RequestBody Verify verify) {
        logger.debug("Email verification request for: {}", verify.getEmail());
        try {
            String result = userService.verify(verify);
            logger.info("Email verified successfully: {}", verify.getEmail());
            return ResponseEntity.ok(result);
        } catch (CustomExceptions.AlreadyVerifiedEmail e) {
            logger.warn("Email already verified: {}", verify.getEmail());
            return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(e.getMessage());
        } catch (CustomExceptions.InvalidPinExceptions e) {
            logger.warn("Invalid PIN for email verification: {}", verify.getEmail());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Email verification failed for: {}", verify.getEmail(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }


    @DeleteMapping("/delete/account")
    public ResponseEntity<String> delete(@RequestBody DeleteAccount deleteAccount) {
        logger.debug("Account deletion request for: {}", deleteAccount.getEmail());
        try {
            userService.delete(deleteAccount);
            logger.info("Account deleted successfully: {}", deleteAccount.getEmail());
            return ResponseEntity.ok("Account deleted successfully.");
        } catch (CustomExceptions.UserNotFoundException e) {
            logger.warn("Account deletion failed - user not found: {}", deleteAccount.getEmail());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (CustomExceptions.InvalidPassword e) {
            logger.warn("Account deletion failed - invalid password for: {}", deleteAccount.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Account deletion failed - internal error for: {}", deleteAccount.getEmail(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }

    @DeleteMapping("/delete/verify/{email}")
    public ResponseEntity<String> cancelVerify(@PathVariable String email) {
        logger.debug("Cancel verification request for: {}", email);
        try {
            userService.deleteVerify(email);
            logger.info("Unverified account deleted: {}", email);
            return ResponseEntity.ok("Account deleted successfully.");
        } catch (CustomExceptions.UserNotFoundException e) {
            logger.warn("Cancel verification failed - user not found: {}", email);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Cancel verification failed for: {}", email, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }


    @PostMapping("/like")
    public ResponseEntity<?> like(@RequestParam String liker,
                                  @RequestParam String liked) {
        logger.debug("Like action from {} to {}", liker, liked);
        try {
            Like savedLike = likeService.saveLike(liker, liked);
            LoggingUtil.logUserAction(liker, "LIKE", "Liked user: " + liked);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    Map.of(
                            "status", "success",
                            "message", "Like saved successfully",
                            "data", savedLike
                    )
            );
        } catch (NoSuchElementException e) {
            logger.warn("Like failed - user not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of(
                            "status", "error",
                            "message", "User not found: " + e.getMessage()
                    )
            );
        } catch (DataIntegrityViolationException e) {
            logger.warn("Like failed - already exists: {} -> {}", liker, liked);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    Map.of(
                            "status", "error",
                            "message", "Like already exists"
                    )
            );
        } catch (Exception e) {
            logger.error("Like failed - internal error: {} -> {}", liker, liked, e);
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
        logger.debug("Checking like from {} to {}", liker, liked);
        return likeService.getLike(liker, liked);
    }

    @PutMapping("/update/speciality")
    public ResponseEntity<String> updateSpecialities(@RequestBody SpecialityDto specialityDto) {
        logger.debug("Updating specialities for user: {}", specialityDto.getUserName());
        try {
            String result = specialityService.addSpecialityForUser(specialityDto);
            logger.info("Specialities updated successfully for user: {}", specialityDto.getUserName());
            return ResponseEntity.ok(result);
        } catch (CustomExceptions.UserNotFoundException e) {
            logger.warn("User not found for speciality update: {}", specialityDto.getUserName());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid speciality data for user: {}", specialityDto.getUserName());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error updating specialities for user: {}", specialityDto.getUserName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }


    @PutMapping("/update/socialNetworks")
    public ResponseEntity<String> updateSocialNetworks(@RequestBody SocialNetworksDto socialNetworksDto) {
        logger.debug("Updating social networks for user: {}", socialNetworksDto.getUserName());
        try {
            String result = socialNetworksService.updateSocialNetworks(socialNetworksDto);
            logger.info("Social networks updated successfully for user: {}", socialNetworksDto.getUserName());
            return ResponseEntity.ok(result);
        } catch (CustomExceptions.UserNotFoundException e) {
            logger.warn("User not found for social networks update: {}", socialNetworksDto.getUserName());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (CustomExceptions.InvalidSocialNetworkException e) {
            logger.warn("Invalid social network data for user: {}", socialNetworksDto.getUserName());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error updating social networks for user: {}", socialNetworksDto.getUserName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }


    @PutMapping("/update/aboutMe")
    public ResponseEntity<String> updateAboutMe(@RequestBody AboutMe aboutMe) {
        logger.debug("Updating about me for user: {}", aboutMe.getUserName());
        try {
            String result = userService.updateAboutMe(aboutMe);
            logger.info("About me updated successfully for user: {}", aboutMe.getUserName());
            return ResponseEntity.ok(result);
        } catch (CustomExceptions.UserNotFoundException e) {
            logger.warn("User not found for about me update: {}", aboutMe.getUserName());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/aboutMe/{userName}")
    public ResponseEntity<String> getAboutMe(@PathVariable String userName) {
        logger.debug("Getting about me for user: {}", userName);
        try {
            String result = userService.getUserByUserName(userName).get().getAboutMe();
            return ResponseEntity.ok(result);
        } catch (CustomExceptions.UserNotFoundException e) {
            logger.warn("User not found for about me: {}", userName);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/searchByHobbies/{userName}")
    public ResponseEntity<?> findUsersByHobbies(@PathVariable String userName) {
        logger.debug("Searching users by hobbies for: {}", userName);
        try {
            SearchUser searchUser = userService.findUsersByHobbies(userName);
            LoggingUtil.logUserAction(userName, "SEARCH_BY_HOBBIES", "Found user: " + searchUser.getUserName());
            return ResponseEntity.ok(searchUser);
        } catch (CustomExceptions.UserNotFoundException e) {
            logger.warn("User not found for hobby search: {}", userName);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid search data for user: {}", userName);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error searching by hobbies for user: {}", userName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }

    @GetMapping("/searchBySpecialities/{userName}")
    public ResponseEntity<?> findUsersBySpecialities(@PathVariable String userName) {
        logger.debug("Searching users by specialities for: {}", userName);
        try {
            SearchUser searchUser = userService.findUsersBySpecialities(userName);
            LoggingUtil.logUserAction(userName, "SEARCH_BY_SPECIALITIES", "Found user: " + searchUser.getUserName());
            return ResponseEntity.ok(searchUser);
        } catch (CustomExceptions.UserNotFoundException e) {
            logger.warn("User not found for speciality search: {}", userName);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid search data for user: {}", userName);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error searching by specialities for user: {}", userName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }

    @GetMapping("/username/{userName}")
    public ResponseEntity<User> getUserByUserName(@PathVariable String userName) {
        logger.debug("Getting user by username: {}", userName);
        Optional<User> userOptional = userService.getUserByUserName(userName);
        return userOptional
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("image/upload")
    public ResponseEntity<String> uploadProfilePicture(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userName") String userName) {
        logger.debug("Uploading profile picture for user: {}", userName);
        try {
            String filePath = imageService.uploadProfilePicture(file, userName);
            LoggingUtil.logUserAction(userName, "UPLOAD_IMAGE", "Profile picture uploaded");
            logger.info("Profile picture uploaded successfully for user: {}", userName);
            return ResponseEntity.ok("Profile picture uploaded successfully: " + filePath);
        } catch (IllegalArgumentException e) {
            logger.warn("Image upload failed - validation error for {}: {}", userName, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Image upload failed for user: {}", userName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload profile picture: " + e.getMessage());
        }
    }

    @GetMapping("/image/{userName}")
    public ResponseEntity<Resource> getUserImage(@PathVariable String userName) {
        logger.debug("Getting user image for: {}", userName);
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
                logger.warn("Image not found for user: {}", userName);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error getting image for user: {}", userName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/image/delete/{userName}")
    public ResponseEntity<String> deleteUserImage(@PathVariable String userName) {
        logger.debug("Deleting user image for: {}", userName);
        try {
            imageService.deleteUserImage(userName);
            LoggingUtil.logUserAction(userName, "DELETE_IMAGE", "Profile picture deleted");
            logger.info("User image deleted successfully: {}", userName);
            return ResponseEntity.ok("User image deleted successfully.");
        } catch (IllegalArgumentException e) {
            logger.warn("Image deletion failed - user not found: {}", userName);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Image deletion failed for user: {}", userName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete user image: " + e.getMessage());
        }
    }

    @GetMapping("/socialNetworks/{userName}")
    public ResponseEntity<List<SocialNetworksGetterDto>> getAllSocialNetworks(@PathVariable String userName) {
        logger.debug("Getting social networks for user: {}", userName);
        List<SocialNetworksGetterDto> socialNetworks = socialNetworksService.getAllSocialNetworks(userName);
        return new ResponseEntity<>(socialNetworks, HttpStatus.OK);
    }

    @GetMapping("/hobby")
    public List<Hobby> getAllHobbies() {
        logger.debug("Getting all hobbies");
        return hobbyService.getAll();
    }

    @GetMapping("/hobby/{hobbyName}")
    public ResponseEntity<Hobby> getHobbyByName(@PathVariable String hobbyName) {
        logger.debug("Getting hobby by name: {}", hobbyName);
        return ResponseEntity.ok(hobbyService.getHobbyByName(hobbyName)
                .orElseThrow(() -> {
                    logger.warn("Hobby not found: {}", hobbyName);
                    return new RuntimeException("Hobby not found with hobby name: " + hobbyName);
                }));
    }

    @GetMapping("/speciality")
    public List<Speciality> getAllSpecialities() {
        logger.debug("Getting all specialities");
        return specialityService.getAll();
    }

    @GetMapping("/speciality/{specialityName}")
    public ResponseEntity<Speciality> getSpecialityByName(@PathVariable String specialityName) {
        logger.debug("Getting speciality by name: {}", specialityName);
        return ResponseEntity.ok(specialityService.getSpecialityByName(specialityName)
                .orElseThrow(() -> {
                    logger.warn("Speciality not found: {}", specialityName);
                    return new RuntimeException("Speciality not found with hobby name: " + specialityName);
                }));
    }

    @GetMapping("/get/specialities/{userName}")
    public ResponseEntity<List<Speciality>> getSpecialitiesByUserName(@PathVariable String userName) {
        logger.debug("Getting specialities for user: {}", userName);
        return userService.getUserByUserName(userName)
                .map(user -> ResponseEntity.ok(user.getSpecialities()))
                .orElseThrow(() -> {
                    logger.warn("User not found for specialities: {}", userName);
                    return new RuntimeException("User not found with user name: " + userName);
                });
    }


    @GetMapping("/get/hobbies/{userName}")
    public ResponseEntity<List<Hobby>> getHobbiesByUserName(@PathVariable String userName) {
        logger.debug("Getting hobbies for user: {}", userName);
        return userService.getUserByUserName(userName)
                .map(user -> ResponseEntity.ok(user.getHobbies()))
                .orElseThrow(() -> {
                    logger.warn("User not found for hobbies: {}", userName);
                    return new RuntimeException("User not found with user name: " + userName);
                });
    }


    @GetMapping("/review/{userName}")
    public List<FeedBacks> getAllFeedbacks(@PathVariable String userName) {
        logger.debug("Getting all feedbacks for user: {}", userName);
        return feedBacksService.getAll(userName);
    }


    @GetMapping("/top")
    public List<String> viewTop() {
        logger.debug("Getting top users");
        return userService.viewTop();
    }

    @PostMapping("/review/add")
    public ResponseEntity<?> addReview(
            @RequestBody Map<String, Object> requestBody) {

        String userName = (String) requestBody.get("userName");
        String message = (String) requestBody.get("message");
        Double rating = ((Number) requestBody.get("rating")).doubleValue();

        logger.debug("Adding review for user: {} with rating: {}", userName, rating);

        Object result = feedBacksService.addReview(userName, message, rating);

        if (result instanceof FeedBacks) {
            LoggingUtil.logUserAction(userName, "ADD_REVIEW", "Review added with rating: " + rating);
            logger.info("Review added successfully for user: {}", userName);
            return ResponseEntity.ok(Collections.singletonMap("message", "Feedback submitted successfully"));
        } else if (result instanceof LocalDate) {
            LocalDate nextAllowedDate = (LocalDate) result;
            logger.warn("Review too early for user: {}, next allowed: {}", userName, nextAllowedDate);
            Map<String, String> response = new HashMap<>();
            response.put("message", "You can leave your next review on or after: " + nextAllowedDate);
            return ResponseEntity.status(HttpStatus.TOO_EARLY)
                    .body(response);
        }

        logger.error("Unexpected result type for review addition for user: {}", userName);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Collections.singletonMap("message", "Unexpected error occurred"));
    }

}