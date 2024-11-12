package com.talk_space.model.domain;


import com.talk_space.model.dto.SignUp;
import com.talk_space.model.enums.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Set;


@Entity
@Table(name = "user_entity")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @NotNull(message = "First name cannot be null")
    @Pattern(regexp = "[A-Z][a-z]+", message = "First name must start with a capital letter followed by lowercase letters")
    @Column(name = "first_name", nullable = false, length = 20)
    private String firstName;


    @NotNull(message = "Last name cannot be null")
    @Pattern(regexp = "[A-Z][a-z]+", message = "Last name must start with a capital letter followed by lowercase letters")
    @Column(name = "last_name", nullable = false, length = 20)
    private String lastName;

    @NotNull(message = "User name cannot be null")
    @Column(name = "user_name", nullable = false, unique = true, length = 20)
    private String userName;

    @Column(name = "birth_date", nullable = false)
    @NotNull(message = "Birth date cannot be empty")
    @PastOrPresent(message = "Birth date must be a past date")
    private LocalDate birthDate;


    @NotNull(message = "Gender cannot be empty")
    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Column(name = "email", nullable = false, unique = true)
    @Email(message = "Email should be valid and can`t be empty")
    private String email;

    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+])[a-zA-Z0-9!@#$%^&*()_+]{8,20}$"
            , message = "The password must contain uppercase and lowercase letters, mathematical symbols and numbers.")
    @Column(name = "password", nullable = false)
    private String password;

    @Pattern(
            regexp = "^(\\+374[1-9]\\d{1,2}\\d{5}|\\+1\\d{10}|\\+7\\d{10})$",
            message = "Invalid phone number. Must be a valid Armenian, USA or Russian phone number."
    )
    @Column(name = "phone_number", length = 12, unique = true)
    private String phoneNumber;

    @CreatedDate
    @Column(name = "created")
    private LocalDate createdDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "zodiac")
    private Zodiac zodiacSign;

    @Column(name = "verify_gmail")
    private Boolean verifyGmail;

    @Column(name = "location")
    private String location;


    @OneToMany(mappedBy = "liker", cascade = CascadeType.ALL)
    private Set<Like> liker;

    @OneToMany(mappedBy = "liked", cascade = CascadeType.ALL)
    private Set<Like> liked;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Chat> chats;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Image> images;

    @ManyToMany
    @JoinTable(
            name = "user_hobbies",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "hobby_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "hobby_id"})
    )
    private List<Hobby> hobbies;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Speciality> specialities;

    @Column(name = "education")
    @Enumerated(EnumType.STRING)
    private Education education;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<SocialNetwork> socialNetwork;


    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private Role role;

    @Pattern(regexp = "^[1-9][0-9]{5}$")
    @Column(name = "pin")
    private String pin;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private Status status;


    public Zodiac getZodiacSign(LocalDate birthDate) {
        int year = birthDate.getYear();

        for (Zodiac zodiac : Zodiac.values()) {
            LocalDate startDate = LocalDate.of(year, zodiac.getMonth(), zodiac.getDay());
            LocalDate endDate = (zodiac.ordinal() < Zodiac.values().length - 1)
                    ? LocalDate.of(year, Zodiac.values()[zodiac.ordinal() + 1].getMonth(), Zodiac.values()[zodiac.ordinal() + 1].getDay()).minusDays(1)
                    : LocalDate.of(year + 1, Zodiac.ARIES.getMonth(), Zodiac.ARIES.getDay()).minusDays(1);
            if (!startDate.isAfter(birthDate) && !endDate.isBefore(birthDate)) {
                return zodiac;
            }
        }
        return null;
    }


    public static  LocalDate validateBirthDate(LocalDate birthDate) {
        LocalDate maxAllowedBirthDate = LocalDate.now().minusYears(16);
        LocalDate minAllowedBirthDate = LocalDate.now().minusYears(100);
        if (birthDate.isAfter(maxAllowedBirthDate) || birthDate.isBefore(minAllowedBirthDate)) {
            throw new IllegalArgumentException("User`s birthday between " + minAllowedBirthDate  + " and " + maxAllowedBirthDate);
        }
        return birthDate;
    }




    public User(SignUp signUp) {
        this.firstName = signUp.getFirstName();
        this.lastName = signUp.getLastName();
        this.userName = signUp.getUserName();
        this.birthDate = validateBirthDate(signUp.getBirthDate());
        this.email = signUp.getEmail();
        this.password = signUp.getPassword();
    }
}
