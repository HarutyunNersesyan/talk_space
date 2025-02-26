package com.talk_space.service;


import com.talk_space.exceptions.CustomExceptions;
import com.talk_space.model.domain.User;
import com.talk_space.model.dto.*;
import com.talk_space.model.dto.fillers.FillUsers;
import com.talk_space.model.enums.Gender;
import com.talk_space.model.enums.Role;
import com.talk_space.model.enums.Status;
import com.talk_space.model.enums.Zodiac;
import com.talk_space.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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


@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    private final AuthenticationManager authenticationManager;

    private final PasswordEncoder passwordEncoder;

    private final ImageService imageService;

    private final MailSenderService mailSenderService;




    @Autowired
    public UserService(UserRepository userRepository, @Lazy AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder, ImageService imageService, @Lazy MailSenderService mailSenderService) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.imageService = imageService;
        this.mailSenderService = mailSenderService;
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
        user.setZodiacSign(Zodiac.fromMonthAndDay(user.getBirthDate().getMonthValue(), user.getBirthDate().getDayOfMonth()));
        user.setVerifyMail(false);
        user.setStatus(Status.ACTIVE);
        return userRepository.save(user);
    }


    public Page<User> getUsersWithPaginationAndSorting(int offset, int pageSize, String field) {
        Page<User> users = userRepository.findAll(PageRequest.of(offset, pageSize).withSort(Sort.by(field)));
        return users;
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
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(), deleteAccount.getPassword()));
        } catch (Exception e) {
            throw new CustomExceptions.InvalidPassword("Invalid password");
        }
        if (!user.getImages().isEmpty()){
            for (int i = 0; i < user.getImages().size(); i++) {
                imageService.deleteImage(user.getUserName(),user.getImages().get(i).getFileName());
            }
        }


        userRepository.delete(user);
    }


    /**
     * @param verify
     * @return
     */
    public String verify(Verify verify) {
        Optional<User> user = findUserByEmail(verify.getEmail());
        if (user.get().getVerifyMail()){
            throw new CustomExceptions.AlreadyVerifiedEmail("Your email is already verified.");
        }
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

        if (!user.getVerifyMail()) {
            throw new CustomExceptions.NotVerifiedMailException("Mail must be verified");
        }

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

        User user = userRepository.findUserByEmail(email).orElseThrow(() -> new RuntimeException("User not found with id: " + email));

        String role = user.getRole().name();

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), Collections.singleton(new SimpleGrantedAuthority(role)));
        return userDetails;
    }

    public Optional<User> findUserByEmail(String email) {
        return userRepository.findUserByEmail(email);
    }

    public String forgotPassword(ForgotPassword forgotPassword) {
        Optional<User> optionalUser = findUserByEmail(forgotPassword.getEmail());
        User existingUser = optionalUser.get();

        if (!existingUser.getVerifyMail()) {
            throw new CustomExceptions.NotVerifiedMailException("Mail must be verified");
        }
        String newPassword = forgotPassword.generatePassword();
        existingUser.setPassword(hashPassword(newPassword));
        update(existingUser);
        mailSenderService.sendEmail(forgotPassword.getEmail(),"New password",newPassword);
        return "New password has been send to email";
    }


    public String updateEducation(EducationDto educationDto) {
        Optional<User> user = userRepository.findUserByUserName(educationDto.getUserName());
        user.get().setEducation(educationDto.getEducation());
        userRepository.save(user.get());
        return ("Education updated successfully");
    }

    public String blockUser(BlockAccount blockAccount) {
        Optional<User> user = userRepository.findUserByUserName(blockAccount.getUserName());
        if (user.isEmpty()) {
            throw new CustomExceptions.UserNotFoundException("User not found.");
        }
        user.get().setStatus(Status.BLOCKED);
        user.get().setBlockedMessage(blockAccount.getBlockMessage());
        userRepository.save(user.get());
        return blockAccount.getBlockMessage();

    }
    public Stack<SearchUser> findUsersByHobbies(User user, int offset, int pageSize) {
        if (user == null || user.getUserName() == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        Optional<User> optionalUser = userRepository.findUserByUserName(user.getUserName());
        if (optionalUser.isEmpty() || optionalUser.get().getHobbies() == null || optionalUser.get().getHobbies().isEmpty()) {
            throw new IllegalArgumentException("User hobbies cannot be empty");
        }
        Stack<SearchUser> searchUser = new Stack<>();
        Pageable pageable = PageRequest.of(offset, pageSize);
        Page<User> users = userRepository.findUsersBySpecialities(user.getUserName(), pageable);
        List<User> userList = new ArrayList<>(users.getContent());
        for (int i = 0; i < userList.size(); i++) {
            searchUser.push(new SearchUser(userList.get(i)));
        }

        return searchUser;
    }

    @Transactional
    public Stack<SearchUser> findUsersBySpecialities(User user, int offset, int pageSize) {


        if (user == null || user.getUserName() == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        Optional<User> optionalUser = userRepository.findUserByUserName(user.getUserName());
        if (optionalUser.isEmpty() || optionalUser.get().getSpecialities() == null || optionalUser.get().getSpecialities().isEmpty()) {
            throw new IllegalArgumentException("User`s specialities cannot be empty");

        }
        Stack<SearchUser> searchUser = new Stack<>();
        Pageable pageable = PageRequest.of(offset, pageSize);
        Page<User> users = userRepository.findUsersBySpecialities(user.getUserName(), pageable);
        List<User> userList = new ArrayList<>(users.getContent());
        for (int i = 0; i < userList.size(); i++) {
            searchUser.push(new SearchUser(userList.get(i)));
        }

        return searchUser;
    }



//    @PostConstruct
//    public void fillDB() {
//        List<User> users = new ArrayList<>();
//
//        users.add(new User(new FillUsers("Aram", "Hakobyan", "AramH95",
//                LocalDate.of(1995, 11, 1), "aram.hakob95@gmail.com",
//                hashPassword("Aram_H95#"), Gender.MALE)));
//
//
//        users.add(new User(new FillUsers("Mariam", "Petrosyan", "MariamP97",
//                LocalDate.of(1997, 3, 14), "mariam.pet97@gmail.com",
//                hashPassword("Mariam_P97#"), Gender.FEMALE)));
//
//        users.add(new User(new FillUsers("Narek", "Sargsyan", "NarekS00",
//                LocalDate.of(2000, 6, 23), "narek.sar2000@gmail.com",
//                hashPassword("Narek_S00#"), Gender.MALE)));
//
//        users.add(new User(new FillUsers("Ani", "Ghazaryan", "AniG96",
//                LocalDate.of(1996, 8, 19), "ani.ghaz96@gmail.com",
//                hashPassword("Ani_G96#"), Gender.FEMALE)));
//
//        users.add(new User(new FillUsers("Gor", "Avagyan", "GorA93",
//                LocalDate.of(1993, 1, 9), "gor.ava93@gmail.com",
//                hashPassword("Gor_A93#"), Gender.MALE)));
//
//        users.add(new User(new FillUsers("Siranush", "Harutyunyan", "SiranH98",
//                LocalDate.of(1998, 10, 25), "siran.har98@gmail.com",
//                hashPassword("Siran_H98#"), Gender.FEMALE)));
//
//        users.add(new User(new FillUsers("Tigran", "Khachatryan", "TigranK99",
//                LocalDate.of(1999, 11, 30), "tigran.khac99@gmail.com",
//                hashPassword("Tigran_K99#"), Gender.MALE)));
//
//        users.add(new User(new FillUsers("Lusine", "Hovhannisyan", "LusineH01",
//                LocalDate.of(2001, 5, 21), "lusine.hov01@gmail.com",
//                hashPassword("Lusine_H01#"), Gender.FEMALE)));
//
//        users.add(new User(new FillUsers("Karen", "Mkrtchyan", "KarenM92",
//                LocalDate.of(1992, 2, 11), "karen.mk92@gmail.com",
//                hashPassword("Karen_M92#"), Gender.MALE)));
//
//        users.add(new User(new FillUsers("Hasmik", "Grigoryan", "HasmikG94",
//                LocalDate.of(1994, 7, 16), "hasmik.grig94@gmail.com",
//                hashPassword("Hasmik_G94#"), Gender.FEMALE)));
//
//        users.add(new User(new FillUsers("Vahan", "Martirosyan", "VahanM90",
//                LocalDate.of(1990, 4, 7), "vahan.mar90@gmail.com",
//                hashPassword("Vahan_M90#"), Gender.MALE)));
//
//        users.add(new User(new FillUsers("Elina", "Sahakyan", "ElinaS95",
//                LocalDate.of(1995, 9, 15), "elina.sah95@gmail.com",
//                hashPassword("Elina_S95#"), Gender.FEMALE)));
//
//        users.add(new User(new FillUsers("Artur", "Gevorgyan", "ArturG91",
//                LocalDate.of(1991, 12, 2), "artur.gev91@gmail.com",
//                hashPassword("Artur_G91#"), Gender.MALE)));
//
//        users.add(new User(new FillUsers("Sofya", "Vardanyan", "SofyaV96",
//                LocalDate.of(1996, 6, 18), "sofya.var96@gmail.com",
//                hashPassword("Sofya_V96#"), Gender.FEMALE)));
//
//        users.add(new User(new FillUsers("David", "Karapetyan", "DavidK97",
//                LocalDate.of(1997, 3, 29), "david.kar97@gmail.com",
//                hashPassword("David_K97#"), Gender.MALE)));
//
//        users.add(new User(new FillUsers("Anahit", "Margaryan", "AnahitM98",
//                LocalDate.of(1998, 5, 22), "anahit.mar98@gmail.com",
//                hashPassword("Anahit_M98#"), Gender.FEMALE)));
//
//        users.add(new User(new FillUsers("Hayk", "Ghukasyan", "HaykG93",
//                LocalDate.of(1993, 8, 8), "hayk.ghu93@gmail.com",
//                hashPassword("Hayk_G93#"), Gender.MALE)));
//
//        users.add(new User(new FillUsers("Arpi", "Hakobyan", "ArpiH95",
//                LocalDate.of(1995, 1, 27), "arpi.hak95@gmail.com",
//                hashPassword("Arpi_H95#"), Gender.FEMALE)));
//
//        users.add(new User(new FillUsers("Stepan", "Manukyan", "StepanM92",
//                LocalDate.of(1992, 9, 14), "stepan.man92@gmail.com",
//                hashPassword("Stepan_M92#"), Gender.MALE)));
//
//        users.add(new User(new FillUsers("Margarita", "Simonyan", "MargaS94",
//                LocalDate.of(1994, 2, 4), "margarita.sim94@gmail.com",
//                hashPassword("Marga_S94#"), Gender.FEMALE)));
//        userRepository.saveAll(users);
//    }
}
