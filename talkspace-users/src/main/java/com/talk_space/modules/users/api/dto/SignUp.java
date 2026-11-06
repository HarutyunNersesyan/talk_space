package com.talk_space.modules.users.api.dto;
import com.talk_space.modules.users.api.model.User;


import com.talk_space.modules.users.api.model.Gender;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SignUp {


    @NotNull(message = "First name cannot be null")
    @Pattern(regexp = "[A-Z][a-z]+", message = "First name must start with a capital letter followed by lowercase letters")
    private String firstName;


    @NotNull(message = "Last name cannot be null")
    @Pattern(regexp = "[A-Z][a-z]+", message = "Last name must start with a capital letter followed by lowercase letters")
    private String lastName;

    @NotBlank(message = "User name cannot be empty")
    private String userName;

    @NotNull(message = "Birth date cannot be empty")
    @PastOrPresent(message = "Birth date must be a past date")
    private LocalDate birthDate;


    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Email should be valid")
    @Pattern(
            regexp = "^[a-zA-Z0-9._%+-]+@gmail\\.com$",
            message = "Email should be a valid Gmail address (example@gmail.com)"
    )
    private String email;

    @NotBlank(message = "Password cannot be empty")
    private String password;

    @NotNull(message = "Gender cannot be empty")
    @Enumerated(EnumType.STRING)
    private Gender gender;


}

