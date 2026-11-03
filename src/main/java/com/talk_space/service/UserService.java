package com.talk_space.service;

import com.talk_space.model.domain.User;
import com.talk_space.model.dto.*;
import com.talk_space.model.enums.*;
import com.talk_space.repository.*;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class UserService implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.talk_space.matching.MatchingService matchingService;
    private final com.talk_space.user.service.UserProfileService profiles;
    private final com.talk_space.user.service.UserModerationService moderation;
    private final com.talk_space.user.service.UserRegistrationService registration;
    private final com.talk_space.user.service.UserPasswordService passwords;
    private final com.talk_space.user.service.UserAccountService accounts;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
            com.talk_space.matching.MatchingService matchingService,
            com.talk_space.user.service.UserProfileService profiles,
            com.talk_space.user.service.UserModerationService moderation,
            com.talk_space.user.service.UserRegistrationService registration,
            com.talk_space.user.service.UserPasswordService passwords,
            com.talk_space.user.service.UserAccountService accounts) {
        this.userRepository=userRepository; this.passwordEncoder=passwordEncoder; this.matchingService=matchingService;
        this.profiles=profiles; this.moderation=moderation; this.registration=registration;
        this.passwords=passwords; this.accounts=accounts;
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
        return registration.signUp(user);
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
        return profiles.updateUserByEmail(email, editUser);
    }

    /**
     * @param deleteAccount
     */
    @Transactional
    public void delete(DeleteAccount deleteAccount) {
        accounts.delete(deleteAccount);
    }

    @Transactional
    public void deleteVerify(String email) {
        accounts.deleteVerify(email);
    }

    public String updateAboutMe(AboutMe aboutMe) {
        return profiles.updateAboutMe(aboutMe);
    }


    /**
     * @param verify
     * @return
     */
    public String verify(Verify verify) {
        return registration.verify(verify);
    }


    public String hashPassword(String newPassword) {
        return passwordEncoder.encode(newPassword);
    }

    public Optional<User> findUserByUserName(String userName) {
        logger.debug("Finding user by username: {}", userName);
        return userRepository.findUserByUserName(userName);
    }


    public String changePassword(String email, String oldPassword, String newPassword, String newPasswordRepeat) {
        return passwords.changePassword(email, oldPassword, newPassword, newPasswordRepeat);
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
        return passwords.forgotPassword(forgotPassword);
    }


    public String blockUser(BlockAccount blockAccount) {
        return moderation.blockUser(blockAccount);
    }

    public String unblockUser(String username) {
        return moderation.unblockUser(username);
    }

    public SearchUser findUsersByHobbies(String userName) {
        return matchingService.next(userName, com.talk_space.matching.MatchMode.HOBBIES);
    }

    public SearchUser findUsersBySpecialities(String userName) {
        return matchingService.next(userName, com.talk_space.matching.MatchMode.SPECIALITIES);
    }


    public Optional<User> getUserByUserName(String userName) {
        logger.debug("Getting user by username: {}", userName);
        return userRepository.findUserByUserName(userName);
    }



}