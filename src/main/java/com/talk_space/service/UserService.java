package com.talk_space.service;

import com.talk_space.exceptions.CustomExceptions;
import com.talk_space.model.domain.ChatMessage;
import com.talk_space.model.domain.FeedBacks;
import com.talk_space.model.domain.SocialNetworks;
import com.talk_space.model.domain.User;
import com.talk_space.model.dto.*;
import com.talk_space.model.dto.getters.fillers.FillUsers;
import com.talk_space.model.enums.*;
import com.talk_space.repository.*;
import com.talk_space.util.LoggingUtil;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


@Service
public class UserService implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    private final AuthenticationManager authenticationManager;

    private final PasswordEncoder passwordEncoder;

    private final ImageService imageService;

    private final MailSenderService mailSenderService;

    private final LikeRepository likeRepository;

    private final ChatRepository chatRepository;

    private final SocialNetworksRepository socialNetworksRepository;

    private final FeedBacksRepository feedBacksRepository;

    private final LikesCountRepository likesCountRepository;

    Set<String> myLikes = new HashSet<>();


    @Autowired
    public UserService(UserRepository userRepository, @Lazy AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder, ImageService imageService, @Lazy MailSenderService mailSenderService, LikeRepository likeRepository, ChatRepository chatRepository, SocialNetworksRepository socialNetworksRepository, FeedBacksRepository feedBacksRepository, LikesCountRepository likesCountRepository) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.imageService = imageService;
        this.mailSenderService = mailSenderService;
        this.likeRepository = likeRepository;
        this.chatRepository = chatRepository;
        this.socialNetworksRepository = socialNetworksRepository;
        this.feedBacksRepository = feedBacksRepository;
        this.likesCountRepository = likesCountRepository;
    }


    public Optional<User> getUserByEmail(String email) {
        logger.debug("Fetching user by email: {}", email);
        return userRepository.findUserByEmail(email);
    }

    public Optional<User> getUserById(Long id) {
        logger.debug("Fetching user by ID: {}", id);
        return userRepository.findById(id);
    }

    public List<User> getAll() {
        logger.debug("Fetching all users");
        return userRepository.findAll();
    }

    public User signUp(@Valid User user) {
        LoggingUtil.logInfo(UserService.class, "Starting user signup for email: {}", user.getEmail());

        try {
            user.setCreatedDate(LocalDate.now());
            user.setRole(Role.USER);
            user.setPassword(hashPassword(user.getPassword()));
            user.setZodiacSign(Zodiac.fromMonthAndDay(user.getBirthDate().getMonthValue(), user.getBirthDate().getDayOfMonth()));
            user.setVerifyMail(false);
            user.setStatus(Status.ACTIVE);

            User savedUser = userRepository.save(user);

            LoggingUtil.logUserAction(user.getUserName(), "SIGNUP", "User registered successfully");
            logger.info("User signup completed successfully for: {}", user.getEmail());

            return savedUser;

        } catch (Exception e) {
            LoggingUtil.logError(UserService.class, "Error during user signup for email: {}", user.getEmail(), e);
            throw e;
        }
    }


    public Page<User> getUsersWithPaginationAndSorting(int offset, int pageSize, String field) {
        logger.debug("Fetching users with pagination - offset: {}, pageSize: {}, field: {}", offset, pageSize, field);
        Page<User> users = userRepository.findAll(PageRequest.of(offset, pageSize).withSort(Sort.by(field)));
        return users;
    }


    public User update(User user) {
        logger.debug("Updating user: {}", user.getUserName());
        return userRepository.save(user);
    }

    public User updateUserByEmail(String email, @Valid EditUser editUser) {
        logger.debug("Updating user by email: {}", email);
        Optional<User> optionalUser = userRepository.findUserByEmail(email);
        if (optionalUser.isEmpty()) {
            logger.warn("User not found for email: {}", email);
            throw new IllegalArgumentException("User with email " + email + " not found");
        }
        User user = optionalUser.get();
        LocalDate validatedBirthDate = User.validateBirthDate(editUser.getBirthDate());
        user.setZodiacSign(Zodiac.fromMonthAndDay(user.getBirthDate().getMonthValue(), user.getBirthDate().getDayOfMonth()));
        user.setFirstName(editUser.getFirstName());
        user.setLastName(editUser.getLastName());
        user.setBirthDate(validatedBirthDate);
        user.setAboutMe(editUser.getAboutMe());

        User updatedUser = userRepository.save(user);
        logger.info("User updated successfully: {}", email);
        return updatedUser;
    }

    /**
     * @param deleteAccount
     */
    public void delete(DeleteAccount deleteAccount) {
        logger.debug("Attempting to delete account for email: {}", deleteAccount.getEmail());
        Optional<User> userOptional = userRepository.findUserByEmail(deleteAccount.getEmail());

        if (userOptional.isEmpty()) {
            logger.warn("User not found for deletion: {}", deleteAccount.getEmail());
            throw new CustomExceptions.UserNotFoundException("User not found.");
        }

        User user = userOptional.get();
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(), deleteAccount.getPassword()));
        } catch (Exception e) {
            logger.warn("Invalid password during account deletion for: {}", deleteAccount.getEmail());
            throw new CustomExceptions.InvalidPassword("Invalid password");
        }
        if (user.getImage() != null) {
            imageService.deleteUserImage(user.getUserName());
        }
        if (!user.getSpecialities().isEmpty()) {
            user.getSpecialities().clear();
        }
        if (!user.getHobbies().isEmpty()) {
            user.getHobbies().clear();
        }
        List<ChatMessage> chatMessages = chatRepository.findSenderChatHistory(user.getUserName());
        if (!chatMessages.isEmpty()) {
            chatRepository.deleteAll(chatMessages);
        }
        List<ChatMessage> chatMessageList = chatRepository.findReceiverChatHistory(user.getUserName());
        if (!chatMessageList.isEmpty()) {
            chatRepository.deleteAll(chatMessageList);
        }
        List<SocialNetworks> socialNetworks = socialNetworksRepository.getSocialNetworksByUserUserName(user.getUserName());
        if (!socialNetworks.isEmpty()) {
            socialNetworksRepository.deleteAll(socialNetworks);
        }
        List<FeedBacks> feedBacks = feedBacksRepository.findFeedBacks(user.getUserName());
        if (!feedBacks.isEmpty()) {
            feedBacksRepository.deleteAll(feedBacks);
        }

        userRepository.delete(user);
        logger.info("Account deleted successfully for: {}", deleteAccount.getEmail());
    }

    public void deleteVerify(String email) {
        logger.debug("Deleting unverified user: {}", email);
        Optional<User> userOptional = userRepository.findUserByEmail(email);

        if (userOptional.isEmpty()) {
            logger.warn("User not found for deletion: {}", email);
            throw new CustomExceptions.UserNotFoundException("User not found.");
        }

        userRepository.delete(userOptional.get());
        logger.info("Unverified user deleted: {}", email);
    }

    public String updateAboutMe(AboutMe aboutMe) {
        logger.debug("Updating about me for user: {}", aboutMe.getUserName());
        Optional<User> userOptional = userRepository.findUserByUserName(aboutMe.getUserName());

        if (userOptional.isEmpty()) {
            logger.warn("User not found for about me update: {}", aboutMe.getUserName());
            throw new CustomExceptions.UserNotFoundException("User not found.");
        }
        User user = userOptional.get();

        user.setAboutMe(aboutMe.getAboutMe());

        userRepository.save(user);
        logger.info("About me updated successfully for user: {}", aboutMe.getUserName());

        return "Updated successfully";

    }


    /**
     * @param verify
     * @return
     */
    public String verify(Verify verify) {
        logger.debug("Verifying email: {}", verify.getEmail());
        Optional<User> user = findUserByEmail(verify.getEmail());
        if (user.get().getVerifyMail()) {
            logger.warn("Email already verified: {}", verify.getEmail());
            throw new CustomExceptions.AlreadyVerifiedEmail("Your email is already verified.");
        }
        if (!user.get().getPin().equals(verify.getPin())) {
            logger.warn("Invalid PIN for email: {}", verify.getEmail());
            throw new CustomExceptions.InvalidPinExceptions("Invalid PIN.");
        }
        User existingUser = user.get();
        existingUser.setVerifyMail(true);
        existingUser.setPin(null);
        update(existingUser);
        logger.info("Email verified successfully: {}", verify.getEmail());
        return "Mail verified successfully";
    }


    public String hashPassword(String newPassword) {
        return passwordEncoder.encode(newPassword);
    }

    public Optional<User> findUserByUserName(String userName) {
        logger.debug("Finding user by username: {}", userName);
        return userRepository.findUserByUserName(userName);
    }


    public String changePassword(String email, String oldPassword, String newPassword, String newPasswordRepeat) {
        logger.debug("Attempting password change for user: {}", email);

        Optional<User> optionalUser = userRepository.findUserByEmail(email);
        User user = optionalUser.get();

        if (!user.getVerifyMail()) {
            logger.warn("Password change attempted for unverified email: {}", email);
            throw new CustomExceptions.NotVerifiedMailException("Mail must be verified");
        }

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, oldPassword));
        } catch (Exception e) {
            logger.warn("Invalid old password for user: {}", email);
            throw new CustomExceptions.InvalidOldPasswordException("Old password is incorrect.");
        }

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            logger.warn("New password same as current for user: {}", email);
            throw new CustomExceptions.InvalidNewPasswordException("New password cannot be the same as the current password.");
        }

        if (!newPassword.equals(newPasswordRepeat)) {
            logger.warn("Password mismatch for user: {}", email);
            throw new CustomExceptions.PasswordMismatchException("New passwords do not match.");
        }

        user.setPassword(hashPassword(newPassword));
        userRepository.save(user);
        logger.info("Password changed successfully for user: {}", email);

        return "Password updated successfully.";
    }


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        logger.debug("Loading user by username (email): {}", email);

        User user = userRepository.findUserByEmail(email).orElseThrow(() -> {
            logger.error("User not found for authentication: {}", email);
            return new RuntimeException("User not found with id: " + email);
        });

        String role = user.getRole().name();

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), Collections.singleton(new SimpleGrantedAuthority(role)));
        return userDetails;
    }

    public Optional<User> findUserByEmail(String email) {
        logger.debug("Finding user by email: {}", email);
        return userRepository.findUserByEmail(email);
    }

    public String forgotPassword(ForgotPassword forgotPassword) {
        logger.debug("Processing forgot password for: {}", forgotPassword.getEmail());
        Optional<User> optionalUser = findUserByEmail(forgotPassword.getEmail());
        User existingUser = optionalUser.get();

        if (!existingUser.getVerifyMail()) {
            logger.warn("Forgot password attempted for unverified email: {}", forgotPassword.getEmail());
            throw new CustomExceptions.NotVerifiedMailException("Mail must be verified");
        }
        String newPassword = forgotPassword.generatePassword();
        existingUser.setPassword(hashPassword(newPassword));
        update(existingUser);
        mailSenderService.sendEmail(forgotPassword.getEmail(), "New password", newPassword);
        logger.info("New password sent to email: {}", forgotPassword.getEmail());
        return "New password has been send to email";
    }


    public String blockUser(BlockAccount blockAccount) {
        logger.debug("Blocking user: {}", blockAccount.getUserName());
        Optional<User> user = userRepository.findUserByUserName(blockAccount.getUserName());
        if (user.isEmpty()) {
            logger.warn("User not found for blocking: {}", blockAccount.getUserName());
            throw new CustomExceptions.UserNotFoundException("User not found.");
        }
        user.get().setStatus(Status.BLOCKED);
        user.get().setBlockedMessage(blockAccount.getBlockMessage());
        user.get().setUntilBlockedDate(blockAccount.getBlockUntil());
        userRepository.save(user.get());
        logger.info("User blocked: {} until {}", blockAccount.getUserName(), blockAccount.getBlockUntil());
        return "User blocked until " + blockAccount.getBlockUntil() + ". Reason: " + blockAccount.getBlockMessage();
    }

    public String unblockUser(String username) {
        logger.debug("Unblocking user: {}", username);
        Optional<User> user = userRepository.findUserByUserName(username);
        if (user.isEmpty()) {
            logger.warn("User not found for unblocking: {}", username);
            throw new CustomExceptions.UserNotFoundException("User not found.");
        }
        user.get().setStatus(Status.BLOCKED);
        userRepository.save(user.get());
        logger.info("User unblocked: {}", username);
        return "User has been unblocked successfully";
    }

    public List<String> viewTop() {
        logger.debug("Fetching top users by likes");
        return likesCountRepository.findTop10LikesCounts().stream()
                .map(likesCount -> likesCount.getUser().getUserName())
                .collect(Collectors.toList());
    }


    @Transactional
    public SearchUser findUsersByHobbies(String userName) {
        logger.debug("Finding users by hobbies for: {}", userName);
        Optional<User> optionalUser = userRepository.findUserByUserName(userName);
        if (optionalUser.isEmpty()) {
            logger.warn("User not found for hobby matching: {}", userName);
            throw new CustomExceptions.UserNotFoundException("User not found.");
        }
        User user = optionalUser.get();

        if (user.getHobbies() == null || user.getHobbies().isEmpty()) {
            logger.warn("User hobbies empty for: {}", userName);
            throw new IllegalArgumentException("User hobbies cannot be empty");
        }

        if (!myLikes.isEmpty()) {
            myLikes = likeRepository.findLikesByLiker(userName);
        }
        Optional<User> nextUser = userRepository.findNextUserByHobbies(user.getUserName(), myLikes);

        if (nextUser.isEmpty()) {
            logger.warn("No users found by hobbies for: {}", userName);
            throw new CustomExceptions.UserNotFoundException("Profiles are out of date, please try again later.");
        }
        myLikes.add(nextUser.get().getUserName());
        logger.info("Found user by hobbies: {} for {}", nextUser.get().getUserName(), userName);
        return new SearchUser(nextUser.get());
    }

    @Transactional
    public SearchUser findUsersBySpecialities(String userName) {
        logger.debug("Finding users by specialities for: {}", userName);
        Optional<User> optionalUser = userRepository.findUserByUserName(userName);
        if (optionalUser.isEmpty()) {
            logger.warn("User not found for speciality matching: {}", userName);
            throw new CustomExceptions.UserNotFoundException("User not found.");
        }
        User user = optionalUser.get();
        if (optionalUser.get().getSpecialities() == null || optionalUser.get().getSpecialities().isEmpty()) {
            logger.warn("User specialities empty for: {}", userName);
            throw new IllegalArgumentException("User specialities cannot be empty");
        }

        if (!myLikes.isEmpty()) {
            myLikes = likeRepository.findLikesByLiker(userName);
        }

        Optional<User> nextUser = userRepository.findNextUserBySpecialities(user.getUserName(), myLikes);
        if (nextUser.isEmpty()) {
            logger.warn("No users found by specialities for: {}", userName);
            throw new CustomExceptions.UserNotFoundException("Profiles are out of date, please try again later.");
        }
        myLikes.add(nextUser.get().getUserName());
        logger.info("Found user by specialities: {} for {}", nextUser.get().getUserName(), userName);
        return new SearchUser(nextUser.get());
    }


    public Optional<User> getUserByUserName(String userName) {
        logger.debug("Getting user by username: {}", userName);
        return userRepository.findUserByUserName(userName);
    }


    @PostConstruct
    public void fillDB() {
        logger.info("Starting to fill database with sample users");
        List<User> users = new ArrayList<>();


        User admin = new User(new FillUsers("Talk", "Space", "admin",
                LocalDate.of(1999, 9, 19), "talkspace783@gmail.com",
                hashPassword("Admin_783#"), Gender.MALE));
        admin.setRole(Role.ADMIN);
        users.add(admin);

        users.add(new User(new FillUsers("Aram", "Hakobyan", "AramH95",
                LocalDate.of(1995, 11, 1), "aram.hakob95@gmail.com",
                hashPassword("Aram_H95#"), Gender.MALE)));


        users.add(new User(new FillUsers("Mariam", "Petrosyan", "MariamP97",
                LocalDate.of(1997, 3, 14), "mariam.pet97@gmail.com",
                hashPassword("Mariam_P97#"), Gender.FEMALE)));


        users.add(new User(new FillUsers("Narek", "Sargsyan", "NarekS00",
                LocalDate.of(2000, 6, 23), "narek.sar2000@gmail.com",
                hashPassword("Narek_S00#"), Gender.MALE)));


        users.add(new User(new FillUsers("Ani", "Ghazaryan", "AniG96",
                LocalDate.of(1996, 8, 19), "ani.ghaz96@gmail.com",
                hashPassword("Ani_G96#"), Gender.FEMALE)));


        users.add(new User(new FillUsers("Gor", "Avagyan", "GorA93",
                LocalDate.of(1993, 1, 9), "gor.ava93@gmail.com",
                hashPassword("Gor_A93#"), Gender.MALE)));

        users.add(new User(new FillUsers("Siranush", "Harutyunyan", "SiranH98",
                LocalDate.of(1998, 10, 25), "siran.har98@gmail.com",
                hashPassword("Siran_H98#"), Gender.FEMALE)));


        users.add(new User(new FillUsers("Tigran", "Khachatryan", "TigranK99",
                LocalDate.of(1999, 11, 30), "tigran.khac99@gmail.com",
                hashPassword("Tigran_K99#"), Gender.MALE)));


        users.add(new User(new FillUsers("Lusine", "Hovhannisyan", "LusineH01",
                LocalDate.of(2001, 5, 21), "lusine.hov01@gmail.com",
                hashPassword("Lusine_H01#"), Gender.FEMALE)));


        users.add(new User(new FillUsers("Karen", "Mkrtchyan", "KarenM92",
                LocalDate.of(1992, 2, 11), "karen.mk92@gmail.com",
                hashPassword("Karen_M92#"), Gender.MALE)));


        users.add(new User(new FillUsers("Hasmik", "Grigoryan", "HasmikG94",
                LocalDate.of(1994, 7, 16), "hasmik.grig94@gmail.com",
                hashPassword("Hasmik_G94#"), Gender.FEMALE)));


        users.add(new User(new FillUsers("Vahan", "Martirosyan", "VahanM90",
                LocalDate.of(1990, 4, 7), "vahan.mar90@gmail.com",
                hashPassword("Vahan_M90#"), Gender.MALE)));


        users.add(new User(new FillUsers("Elina", "Sahakyan", "ElinaS95",
                LocalDate.of(1995, 9, 15), "elina.sah95@gmail.com",
                hashPassword("Elina_S95#"), Gender.FEMALE)));


        users.add(new User(new FillUsers("Artur", "Gevorgyan", "ArturG91",
                LocalDate.of(1991, 12, 2), "artur.gev91@gmail.com",
                hashPassword("Artur_G91#"), Gender.MALE)));


        users.add(new User(new FillUsers("Sofya", "Vardanyan", "SofyaV96",
                LocalDate.of(1996, 6, 18), "sofya.var96@gmail.com",
                hashPassword("Sofya_V96#"), Gender.FEMALE)));


        users.add(new User(new FillUsers("David", "Karapetyan", "DavidK97",
                LocalDate.of(1997, 3, 29), "david.kar97@gmail.com",
                hashPassword("David_K97#"), Gender.MALE)));


        users.add(new User(new FillUsers("Anahit", "Margaryan", "AnahitM98",
                LocalDate.of(1998, 5, 22), "anahit.mar98@gmail.com",
                hashPassword("Anahit_M98#"), Gender.FEMALE)));


        users.add(new User(new FillUsers("Hayk", "Ghukasyan", "HaykG93",
                LocalDate.of(1993, 8, 8), "hayk.ghu93@gmail.com",
                hashPassword("Hayk_G93#"), Gender.MALE)));


        users.add(new User(new FillUsers("Arpi", "Hakobyan", "ArpiH95",
                LocalDate.of(1995, 1, 27), "arpi.hak95@gmail.com",
                hashPassword("Arpi_H95#"), Gender.FEMALE)));


        users.add(new User(new FillUsers("Stepan", "Manukyan", "StepanM92",
                LocalDate.of(1992, 9, 14), "stepan.man92@gmail.com",
                hashPassword("Stepan_M92#"), Gender.MALE)));
        userRepository.saveAll(users);
        logger.info("Database filled with {} sample users", users.size());
    }


}