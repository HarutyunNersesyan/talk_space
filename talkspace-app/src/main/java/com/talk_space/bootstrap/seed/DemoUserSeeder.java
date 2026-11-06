package com.talk_space.bootstrap.seed;

import com.talk_space.modules.users.api.model.User;
import com.talk_space.modules.users.api.dto.FillUsers;
import com.talk_space.modules.users.api.model.Gender;
import com.talk_space.modules.users.api.model.Role;
import com.talk_space.modules.users.internal.persistence.UserRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class DemoUserSeeder {
    private static final Logger logger=LoggerFactory.getLogger(DemoUserSeeder.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    public DemoUserSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository=userRepository; this.passwordEncoder=passwordEncoder;
    }

    @org.springframework.beans.factory.annotation.Value("${app.demo-seed.enabled:true}")
    private boolean demoSeedEnabled = true;

    @PostConstruct
    public void fillDB() {
        if (!demoSeedEnabled || userRepository.count() > 0) return;
        logger.info("Starting to fill database with sample users");
        List<User> users = new ArrayList<>();


        User admin = new User(new FillUsers("Talk", "Space", "admin",
                LocalDate.of(1999, 9, 19), "talkspace783@gmail.com",
                passwordEncoder.encode("Admin_783#"), Gender.MALE));
        admin.setRole(Role.ADMIN);
        users.add(admin);

        users.add(new User(new FillUsers("Aram", "Hakobyan", "AramH95",
                LocalDate.of(1995, 11, 1), "aram.hakob95@gmail.com",
                passwordEncoder.encode("Aram_H95#"), Gender.MALE)));


        users.add(new User(new FillUsers("Mariam", "Petrosyan", "MariamP97",
                LocalDate.of(1997, 3, 14), "mariam.pet97@gmail.com",
                passwordEncoder.encode("Mariam_P97#"), Gender.FEMALE)));


        users.add(new User(new FillUsers("Narek", "Sargsyan", "NarekS00",
                LocalDate.of(2000, 6, 23), "narek.sar2000@gmail.com",
                passwordEncoder.encode("Narek_S00#"), Gender.MALE)));


        users.add(new User(new FillUsers("Ani", "Ghazaryan", "AniG96",
                LocalDate.of(1996, 8, 19), "ani.ghaz96@gmail.com",
                passwordEncoder.encode("Ani_G96#"), Gender.FEMALE)));


        users.add(new User(new FillUsers("Gor", "Avagyan", "GorA93",
                LocalDate.of(1993, 1, 9), "gor.ava93@gmail.com",
                passwordEncoder.encode("Gor_A93#"), Gender.MALE)));

        users.add(new User(new FillUsers("Siranush", "Harutyunyan", "SiranH98",
                LocalDate.of(1998, 10, 25), "siran.har98@gmail.com",
                passwordEncoder.encode("Siran_H98#"), Gender.FEMALE)));


        users.add(new User(new FillUsers("Tigran", "Khachatryan", "TigranK99",
                LocalDate.of(1999, 11, 30), "tigran.khac99@gmail.com",
                passwordEncoder.encode("Tigran_K99#"), Gender.MALE)));


        users.add(new User(new FillUsers("Lusine", "Hovhannisyan", "LusineH01",
                LocalDate.of(2001, 5, 21), "lusine.hov01@gmail.com",
                passwordEncoder.encode("Lusine_H01#"), Gender.FEMALE)));


        users.add(new User(new FillUsers("Karen", "Mkrtchyan", "KarenM92",
                LocalDate.of(1992, 2, 11), "karen.mk92@gmail.com",
                passwordEncoder.encode("Karen_M92#"), Gender.MALE)));


        users.add(new User(new FillUsers("Hasmik", "Grigoryan", "HasmikG94",
                LocalDate.of(1994, 7, 16), "hasmik.grig94@gmail.com",
                passwordEncoder.encode("Hasmik_G94#"), Gender.FEMALE)));


        users.add(new User(new FillUsers("Vahan", "Martirosyan", "VahanM90",
                LocalDate.of(1990, 4, 7), "vahan.mar90@gmail.com",
                passwordEncoder.encode("Vahan_M90#"), Gender.MALE)));


        users.add(new User(new FillUsers("Elina", "Sahakyan", "ElinaS95",
                LocalDate.of(1995, 9, 15), "elina.sah95@gmail.com",
                passwordEncoder.encode("Elina_S95#"), Gender.FEMALE)));


        users.add(new User(new FillUsers("Artur", "Gevorgyan", "ArturG91",
                LocalDate.of(1991, 12, 2), "artur.gev91@gmail.com",
                passwordEncoder.encode("Artur_G91#"), Gender.MALE)));


        users.add(new User(new FillUsers("Sofya", "Vardanyan", "SofyaV96",
                LocalDate.of(1996, 6, 18), "sofya.var96@gmail.com",
                passwordEncoder.encode("Sofya_V96#"), Gender.FEMALE)));


        users.add(new User(new FillUsers("David", "Karapetyan", "DavidK97",
                LocalDate.of(1997, 3, 29), "david.kar97@gmail.com",
                passwordEncoder.encode("David_K97#"), Gender.MALE)));


        users.add(new User(new FillUsers("Anahit", "Margaryan", "AnahitM98",
                LocalDate.of(1998, 5, 22), "anahit.mar98@gmail.com",
                passwordEncoder.encode("Anahit_M98#"), Gender.FEMALE)));


        users.add(new User(new FillUsers("Hayk", "Ghukasyan", "HaykG93",
                LocalDate.of(1993, 8, 8), "hayk.ghu93@gmail.com",
                passwordEncoder.encode("Hayk_G93#"), Gender.MALE)));


        users.add(new User(new FillUsers("Arpi", "Hakobyan", "ArpiH95",
                LocalDate.of(1995, 1, 27), "arpi.hak95@gmail.com",
                passwordEncoder.encode("Arpi_H95#"), Gender.FEMALE)));


        users.add(new User(new FillUsers("Stepan", "Manukyan", "StepanM92",
                LocalDate.of(1992, 9, 14), "stepan.man92@gmail.com",
                passwordEncoder.encode("Stepan_M92#"), Gender.MALE)));
        userRepository.saveAll(users);
        logger.info("Database filled with {} sample users", users.size());
    }


}
