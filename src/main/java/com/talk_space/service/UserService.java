package com.talk_space.service;


import com.talk_space.exceptions.CustomExceptions;
import com.talk_space.model.domain.User;
import com.talk_space.model.dto.*;
import com.talk_space.model.enums.Role;
import com.talk_space.model.enums.Status;
import com.talk_space.repository.UserRepository;
import com.talk_space.validation.PhoneNumberValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.*;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    private final AuthenticationManager authenticationManager;

    private final PasswordEncoder passwordEncoder;


    @Autowired
    public UserService(UserRepository userRepository, @Lazy AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
    }


    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public User signUp(User user) {
        user.setCreatedDate(LocalDate.now());
        user.setRole(Role.USER);
        user.setPassword(hashPassword(user.getPassword()));
        user.setZodiacSign(user.getZodiacSign(user.getBirthDate()));
        user.setVerifyMail(false);
        user.setStatus(Status.ACTIVE);
        user.setIsBlocked(false);
        return userRepository.save(user);
    }

    public User update(User user) {
        return userRepository.save(user);
    }

    /**
     * @param deleteAccount
     */
    public void delete(DeleteAccount deleteAccount) {
        Optional<User> userOptional = userRepository.findUserByUserName(deleteAccount.getUserName());

        if (userOptional.isEmpty()) {
            throw new CustomExceptions.UserNotFoundException("User not found.");
        }

        User user = userOptional.get();
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getEmail(), deleteAccount.getPassword())
            );
        } catch (Exception e) {
            throw new CustomExceptions.InvalidPassword("Invalid password");
        }

        user.setStatus(Status.DELETED);
        userRepository.save(user);
    }


    /**
     * @param verify
     * @return
     */

    public String verify(Verify verify) {
        Optional<User> user = findUserByEmail(verify.getEmail());
        if (!user.get().getPin().equals(verify.getPin())) {
            throw new CustomExceptions.InvalidPinExceptions("Invalid PIN.");
        }
        User existingUser = user.get();
        existingUser.setVerifyMail(true);
        update(existingUser);

        return "Mail Verified successfully";
    }


    public String hashPassword(String newPassword) {
        return passwordEncoder.encode(newPassword);
    }


    public String changePassword(String email, String oldPassword, String newPassword, String newPasswordRepeat) {

        Optional<User> optionalUser = userRepository.findUserByEmail(email);
        User user = optionalUser.get();

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, oldPassword));
        } catch (Exception e) {
            throw new CustomExceptions.InvalidOldPasswordException("Old password is incorrect.");
        }

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new CustomExceptions.InvalidNewPasswordException("New password cannot be the same as the current password.");
        }

        if (!newPassword.equals(newPasswordRepeat)) {
            throw new CustomExceptions.PasswordMismatchException("New passwords do not match.");
        }

        user.setPassword(hashPassword(newPassword));
        userRepository.save(user);

        return "Password updated successfully.";
    }


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user = userRepository.findUserByEmail(email).
                orElseThrow(() -> new RuntimeException("Customer not found with id: " + email));

        String role = user.getRole().name();

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singleton(new SimpleGrantedAuthority(role)));
        return userDetails;
    }

    public Optional<User> findUserByEmail(String email) {
        return userRepository.findUserByEmail(email);
    }

    public String forgotPassword(ForgotPassword forgotPassword) {

        Optional<User> optionalUser = findUserByEmail(forgotPassword.getEmail());
        User existingUser = optionalUser.get();

        if (!existingUser.getPin().equals(forgotPassword.getPin())) {
            throw new CustomExceptions.InvalidPinExceptions("Invalid PIN.");
        }

        if (passwordEncoder.matches(forgotPassword.getNewPassword(), existingUser.getPassword())) {
            throw new CustomExceptions.InvalidNewPasswordException("New password cannot be the same as the current password.");
        }
        existingUser.setPassword(hashPassword(forgotPassword.getNewPassword()));
        update(existingUser);

        return "Password updated successfully.";
    }

    public String updatePhoneNumber(PhoneNumberDto phoneNumberDto) {

        Optional<User> user = userRepository.findUserByUserName(phoneNumberDto.getUserName());

        boolean isValid = PhoneNumberValidator.isValidPhoneNumber(phoneNumberDto.getPhoneNumber(), phoneNumberDto.getCountry());

        if (!isValid) {
            throw new CustomExceptions.InvalidPhoneNumberException("Invalid phone number. Must be a valid number for " + phoneNumberDto.getCountry() + ".");
        }
        user.get().setPhoneNumber(phoneNumberDto.getPhoneNumber());
        userRepository.save(user.get());

        return ("Phone number updated successfully");
    }


    public String updateEducation(EducationDto educationDto) {
        Optional<User> user = userRepository.findUserByUserName(educationDto.getUserName());
        user.get().setEducation(educationDto.getEducation());
        userRepository.save(user.get());
        return ("Education updated successfully");
    }

//    public List<UserBasicInfo> getActiveUserBasicInfo() {
//        return userRepository.getBasicInfo();
//    }
}
