package com.talk_space.service;


import com.talk_space.exceptions.CustomExceptions;
import com.talk_space.model.domain.User;
import com.talk_space.model.dto.*;
import com.talk_space.model.dto.getters.fillers.FillUsers;
import com.talk_space.model.enums.*;
import com.talk_space.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
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


@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    private final AuthenticationManager authenticationManager;

    private final PasswordEncoder passwordEncoder;

    private final ImageService imageService;

    private final MailSenderService mailSenderService;

    private Map<String, Set<String>> existingUsersForSpecialities = new ConcurrentHashMap<>();
    private Map<String, Set<String>> existingUsersForHobbies = new ConcurrentHashMap<>();


    @Autowired
    public UserService(UserRepository userRepository, @Lazy AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder, ImageService imageService, @Lazy MailSenderService mailSenderService) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.imageService = imageService;
        this.mailSenderService = mailSenderService;
    }


    public Optional<User> getUserByEmail(String email) {
        return userRepository.findUserByEmail(email);
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public User signUp(@Valid User user) {
        user.setCreatedDate(LocalDate.now());
        user.setRole(Role.USER);
        user.setPassword(hashPassword(user.getPassword()));
        user.setZodiacSign(Zodiac.fromMonthAndDay(user.getBirthDate().getMonthValue(), user.getBirthDate().getDayOfMonth()));
        user.setVerifyMail(false);
        user.setStatus(Status.ACTIVE);
        Set<String> existingUsersH = new HashSet<>();
        Set<String> existingUsersS = new HashSet<>();
        existingUsersH.add(user.getUserName());
        existingUsersS.add(user.getUserName());
        existingUsersForHobbies.put(user.getUserName(), existingUsersH);
        existingUsersForSpecialities.put(user.getUserName(), existingUsersS);
        return userRepository.save(user);
    }


    public Page<User> getUsersWithPaginationAndSorting(int offset, int pageSize, String field) {
        Page<User> users = userRepository.findAll(PageRequest.of(offset, pageSize).withSort(Sort.by(field)));
        return users;
    }


    public User update(User user) {
        return userRepository.save(user);
    }

    public User updateUserByEmail(String email, @Valid EditUser editUser) {
        Optional<User> optionalUser = userRepository.findUserByEmail(email);
        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException("User with email " + email + " not found");
        }
        User user = optionalUser.get();
        LocalDate validatedBirthDate = User.validateBirthDate(editUser.getBirthDate());
        user.setZodiacSign(Zodiac.fromMonthAndDay(user.getBirthDate().getMonthValue(), user.getBirthDate().getDayOfMonth()));
        user.setFirstName(editUser.getFirstName());
        user.setLastName(editUser.getLastName());
        user.setBirthDate(validatedBirthDate);
        user.setAboutMe(editUser.getAboutMe());
        return userRepository.save(user);
    }

    /**
     * @param deleteAccount
     */
    public void delete(DeleteAccount deleteAccount) {
        Optional<User> userOptional = userRepository.findUserByEmail(deleteAccount.getEmail());

        if (userOptional.isEmpty()) {
            throw new CustomExceptions.UserNotFoundException("User not found.");
        }

        User user = userOptional.get();
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(), deleteAccount.getPassword()));
        } catch (Exception e) {
            throw new CustomExceptions.InvalidPassword("Invalid password");
        }
        if (user.getImage() != null) {
            imageService.deleteUserImage(user.getUserName());
        }

        existingUsersForHobbies.get(deleteAccount.getEmail()).clear();
        existingUsersForSpecialities.get(deleteAccount.getEmail()).clear();

        userRepository.delete(user);
    }

    public void deleteVerify(String email) {
        Optional<User> userOptional = userRepository.findUserByEmail(email);

        if (userOptional.isEmpty()) {
            throw new CustomExceptions.UserNotFoundException("User not found.");
        }

        userRepository.delete(userOptional.get());
    }

    public String updateAboutMe(AboutMe aboutMe) {
        Optional<User> userOptional = userRepository.findUserByUserName(aboutMe.getUserName());

        if (userOptional.isEmpty()) {
            throw new CustomExceptions.UserNotFoundException("User not found.");
        }
        User user = userOptional.get();

        user.setAboutMe(aboutMe.getAboutMe());

        userRepository.save(user);

        return "Updated successfully";

    }


    /**
     * @param verify
     * @return
     */
    public String verify(Verify verify) {
        Optional<User> user = findUserByEmail(verify.getEmail());
        if (user.get().getVerifyMail()) {
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

    public Optional<User> findUserByUserName(String userName) {
        return userRepository.findUserByUserName(userName);
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
        mailSenderService.sendEmail(forgotPassword.getEmail(), "New password", newPassword);
        return "New password has been send to email";
    }


    public String blockUser(BlockAccount blockAccount) {
        Optional<User> user = userRepository.findUserByUserName(blockAccount.getUserName());
        if (user.isEmpty()) {
            throw new CustomExceptions.UserNotFoundException("User not found.");
        }
        user.get().setStatus(Status.BLOCKED);
        user.get().setBlockedMessage(blockAccount.getBlockMessage());
        user.get().setUntilBlockedDate(blockAccount.getBlockUntil());
        userRepository.save(user.get());
        return "User blocked until " + blockAccount.getBlockUntil() + ". Reason: " + blockAccount.getBlockMessage();
    }

    public String unblockUser(String username) {
        Optional<User> user = userRepository.findUserByUserName(username);
        if (user.isEmpty()) {
            throw new CustomExceptions.UserNotFoundException("User not found.");
        }
        user.get().setStatus(Status.BLOCKED);
        userRepository.save(user.get());
        return "User has been unblocked successfully";
    }




    @Transactional
    public SearchUser findUsersByHobbies(String userName) {
        Optional<User> optionalUser = userRepository.findUserByUserName(userName);
        if (optionalUser.isEmpty()) {
            throw new CustomExceptions.UserNotFoundException("User not found.");
        }
        User user = optionalUser.get();

        if (user.getHobbies() == null || user.getHobbies().isEmpty()) {
            throw new IllegalArgumentException("User hobbies cannot be empty");
        }


        Set<String> users = existingUsersForHobbies.get(user.getUserName());

        Optional<User> nextUser = userRepository.findNextUserByHobbies(user.getUserName(), users);

        if (nextUser.isEmpty()) {
            throw new CustomExceptions.UserNotFoundException("Profiles are out of date, please try again later.");
        }
        SearchUser searchUser = new SearchUser(nextUser.get());
        existingUsersForHobbies.get(user.getUserName()).add(nextUser.get().getUserName());
        return searchUser;
    }

    @Transactional
    public SearchUser findUsersBySpecialities(String userName) {
        Optional<User> optionalUser = userRepository.findUserByUserName(userName);
        if (optionalUser.isEmpty()) {
            throw new CustomExceptions.UserNotFoundException("User not found.");
        }
        User user = optionalUser.get();
        if (optionalUser.get().getSpecialities() == null || optionalUser.get().getSpecialities().isEmpty()) {
            throw new IllegalArgumentException("User specialities cannot be empty");
        }

        Set<String> users = existingUsersForSpecialities.get(user.getUserName());

        Optional<User> nextUser = userRepository.findNextUserBySpecialities(user.getUserName(), users);

        if (nextUser.isEmpty()) {
            throw new CustomExceptions.UserNotFoundException("Profiles are out of date, please try again later.");
        }
        SearchUser searchUser = new SearchUser(nextUser.get());
        existingUsersForSpecialities.get(user.getUserName()).add(nextUser.get().getUserName());
        return searchUser;
    }


    public Optional<User> getUserByUserName(String userName) {
        return userRepository.findUserByUserName(userName);
    }


    @PostConstruct
    public void fillDB() {
        List<User> users = new ArrayList<>();
        Set<String> existingUsersForS;
        Set<String> existingUsersForH;
        users.add(new User(new FillUsers("Aram", "Hakobyan", "AramH95",
                LocalDate.of(1995, 11, 1), "aram.hakob95@gmail.com",
                hashPassword("Aram_H95#"), Gender.MALE)));
        existingUsersForS = new TreeSet<>();
        existingUsersForH = new TreeSet<>();
        existingUsersForS.add("AramH95");
        existingUsersForH.add("AramH95");
        existingUsersForSpecialities.put("AramH95", existingUsersForS);
        existingUsersForHobbies.put("AramH95", existingUsersForH);


        users.add(new User(new FillUsers("Mariam", "Petrosyan", "MariamP97",
                LocalDate.of(1997, 3, 14), "mariam.pet97@gmail.com",
                hashPassword("Mariam_P97#"), Gender.FEMALE)));
        existingUsersForS = new HashSet<>();
        existingUsersForH = new HashSet<>();
        existingUsersForS.add("MariamP97");
        existingUsersForH.add("MariamP97");
        existingUsersForSpecialities.put("MariamP97", existingUsersForS);
        existingUsersForHobbies.put("MariamP97", existingUsersForH);

        users.add(new User(new FillUsers("Narek", "Sargsyan", "NarekS00",
                LocalDate.of(2000, 6, 23), "narek.sar2000@gmail.com",
                hashPassword("Narek_S00#"), Gender.MALE)));
        existingUsersForS = new HashSet<>();
        existingUsersForH = new HashSet<>();
        existingUsersForS.add("NarekS00");
        existingUsersForH.add("NarekS00");
        existingUsersForSpecialities.put("NarekS00", existingUsersForS);
        existingUsersForHobbies.put("NarekS00", existingUsersForH);


        users.add(new User(new FillUsers("Ani", "Ghazaryan", "AniG96",
                LocalDate.of(1996, 8, 19), "ani.ghaz96@gmail.com",
                hashPassword("Ani_G96#"), Gender.FEMALE)));
        existingUsersForS = new HashSet<>();
        existingUsersForH = new HashSet<>();
        existingUsersForS.add("AniG96");
        existingUsersForH.add("AniG96");
        existingUsersForSpecialities.put("AniG96", existingUsersForS);
        existingUsersForHobbies.put("AniG96", existingUsersForH);

        users.add(new User(new FillUsers("Gor", "Avagyan", "GorA93",
                LocalDate.of(1993, 1, 9), "gor.ava93@gmail.com",
                hashPassword("Gor_A93#"), Gender.MALE)));
        existingUsersForS = new HashSet<>();
        existingUsersForH = new HashSet<>();
        existingUsersForS.add("GorA93");
        existingUsersForH.add("GorA93");
        existingUsersForSpecialities.put("GorA93", existingUsersForS);
        existingUsersForHobbies.put("GorA93", existingUsersForH);

        users.add(new User(new FillUsers("Siranush", "Harutyunyan", "SiranH98",
                LocalDate.of(1998, 10, 25), "siran.har98@gmail.com",
                hashPassword("Siran_H98#"), Gender.FEMALE)));
        existingUsersForS = new HashSet<>();
        existingUsersForH = new HashSet<>();
        existingUsersForS.add("SiranH98");
        existingUsersForH.add("SiranH98");
        existingUsersForSpecialities.put("SiranH98", existingUsersForS);
        existingUsersForHobbies.put("SiranH98", existingUsersForH);


        users.add(new User(new FillUsers("Tigran", "Khachatryan", "TigranK99",
                LocalDate.of(1999, 11, 30), "tigran.khac99@gmail.com",
                hashPassword("Tigran_K99#"), Gender.MALE)));
        existingUsersForS = new HashSet<>();
        existingUsersForH = new HashSet<>();
        existingUsersForS.add("TigranK99");
        existingUsersForH.add("TigranK99");
        existingUsersForSpecialities.put("TigranK99", existingUsersForS);
        existingUsersForHobbies.put("TigranK99", existingUsersForH);

        users.add(new User(new FillUsers("Lusine", "Hovhannisyan", "LusineH01",
                LocalDate.of(2001, 5, 21), "lusine.hov01@gmail.com",
                hashPassword("Lusine_H01#"), Gender.FEMALE)));
        existingUsersForS = new HashSet<>();
        existingUsersForH = new HashSet<>();
        existingUsersForS.add("LusineH01");
        existingUsersForH.add("TLusineH01");
        existingUsersForSpecialities.put("LusineH01", existingUsersForS);
        existingUsersForHobbies.put("LusineH01", existingUsersForH);


        users.add(new User(new FillUsers("Karen", "Mkrtchyan", "KarenM92",
                LocalDate.of(1992, 2, 11), "karen.mk92@gmail.com",
                hashPassword("Karen_M92#"), Gender.MALE)));
        existingUsersForS = new HashSet<>();
        existingUsersForH = new HashSet<>();
        existingUsersForS.add("KarenM92");
        existingUsersForH.add("KarenM92");
        existingUsersForSpecialities.put("KarenM92", existingUsersForS);
        existingUsersForHobbies.put("KarenM92", existingUsersForH);


        users.add(new User(new FillUsers("Hasmik", "Grigoryan", "HasmikG94",
                LocalDate.of(1994, 7, 16), "hasmik.grig94@gmail.com",
                hashPassword("Hasmik_G94#"), Gender.FEMALE)));
        existingUsersForS = new HashSet<>();
        existingUsersForH = new HashSet<>();
        existingUsersForS.add("HasmikG94");
        existingUsersForH.add("HasmikG94");
        existingUsersForSpecialities.put("HasmikG94", existingUsersForS);
        existingUsersForHobbies.put("HasmikG94", existingUsersForH);


        users.add(new User(new FillUsers("Vahan", "Martirosyan", "VahanM90",
                LocalDate.of(1990, 4, 7), "vahan.mar90@gmail.com",
                hashPassword("Vahan_M90#"), Gender.MALE)));
        existingUsersForS = new HashSet<>();
        existingUsersForH = new HashSet<>();
        existingUsersForS.add("VahanM90");
        existingUsersForH.add("VahanM90");
        existingUsersForSpecialities.put("VahanM90", existingUsersForS);
        existingUsersForHobbies.put("VahanM90", existingUsersForH);


        users.add(new User(new FillUsers("Elina", "Sahakyan", "ElinaS95",
                LocalDate.of(1995, 9, 15), "elina.sah95@gmail.com",
                hashPassword("Elina_S95#"), Gender.FEMALE)));
        existingUsersForS = new HashSet<>();
        existingUsersForH = new HashSet<>();
        existingUsersForS.add("ElinaS95");
        existingUsersForH.add("ElinaS95");
        existingUsersForSpecialities.put("ElinaS95", existingUsersForS);
        existingUsersForHobbies.put("ElinaS95", existingUsersForH);


        users.add(new User(new FillUsers("Artur", "Gevorgyan", "ArturG91",
                LocalDate.of(1991, 12, 2), "artur.gev91@gmail.com",
                hashPassword("Artur_G91#"), Gender.MALE)));
        existingUsersForS = new HashSet<>();
        existingUsersForH = new HashSet<>();
        existingUsersForS.add("ArturG91");
        existingUsersForH.add("ArturG91");
        existingUsersForSpecialities.put("ArturG91", existingUsersForS);
        existingUsersForHobbies.put("ArturG91", existingUsersForH);


        users.add(new User(new FillUsers("Sofya", "Vardanyan", "SofyaV96",
                LocalDate.of(1996, 6, 18), "sofya.var96@gmail.com",
                hashPassword("Sofya_V96#"), Gender.FEMALE)));
        existingUsersForS = new HashSet<>();
        existingUsersForH = new HashSet<>();
        existingUsersForS.add("SofyaV96");
        existingUsersForH.add("SofyaV96");
        existingUsersForSpecialities.put("SofyaV96", existingUsersForS);
        existingUsersForHobbies.put("SofyaV96", existingUsersForH);


        users.add(new User(new FillUsers("David", "Karapetyan", "DavidK97",
                LocalDate.of(1997, 3, 29), "david.kar97@gmail.com",
                hashPassword("David_K97#"), Gender.MALE)));
        existingUsersForS = new HashSet<>();
        existingUsersForH = new HashSet<>();
        existingUsersForS.add("DavidK97");
        existingUsersForH.add("DavidK97");
        existingUsersForSpecialities.put("DavidK97", existingUsersForS);
        existingUsersForHobbies.put("DavidK97", existingUsersForH);

        users.add(new User(new FillUsers("Anahit", "Margaryan", "AnahitM98",
                LocalDate.of(1998, 5, 22), "anahit.mar98@gmail.com",
                hashPassword("Anahit_M98#"), Gender.FEMALE)));
        existingUsersForS = new HashSet<>();
        existingUsersForH = new HashSet<>();
        existingUsersForS.add("AnahitM98");
        existingUsersForH.add("AnahitM98");
        existingUsersForSpecialities.put("AnahitM98", existingUsersForS);
        existingUsersForHobbies.put("AnahitM98", existingUsersForH);


        users.add(new User(new FillUsers("Hayk", "Ghukasyan", "HaykG93",
                LocalDate.of(1993, 8, 8), "hayk.ghu93@gmail.com",
                hashPassword("Hayk_G93#"), Gender.MALE)));
        existingUsersForS = new HashSet<>();
        existingUsersForH = new HashSet<>();
        existingUsersForS.add("HaykG93");
        existingUsersForH.add("HaykG93");
        existingUsersForSpecialities.put("HaykG93", existingUsersForS);
        existingUsersForHobbies.put("HaykG93", existingUsersForH);

        users.add(new User(new FillUsers("Arpi", "Hakobyan", "ArpiH95",
                LocalDate.of(1995, 1, 27), "arpi.hak95@gmail.com",
                hashPassword("Arpi_H95#"), Gender.FEMALE)));
        existingUsersForS = new HashSet<>();
        existingUsersForH = new HashSet<>();
        existingUsersForS.add("ArpiH95");
        existingUsersForH.add("ArpiH95");
        existingUsersForSpecialities.put("ArpiH95", existingUsersForS);
        existingUsersForHobbies.put("ArpiH95", existingUsersForH);


        users.add(new User(new FillUsers("Stepan", "Manukyan", "StepanM92",
                LocalDate.of(1992, 9, 14), "stepan.man92@gmail.com",
                hashPassword("Stepan_M92#"), Gender.MALE)));
        existingUsersForS = new HashSet<>();
        existingUsersForH = new HashSet<>();
        existingUsersForS.add("StepanM92");
        existingUsersForH.add("StepanM92");
        existingUsersForSpecialities.put("StepanM92", existingUsersForS);
        existingUsersForHobbies.put("StepanM92", existingUsersForH);
        userRepository.saveAll(users);
    }


}
