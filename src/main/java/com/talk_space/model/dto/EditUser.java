package com.talk_space.model.dto;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EditUser {

        @NotNull(message = "First name cannot be null")
        @Pattern(regexp = "[A-Z][a-z]+", message = "First name must start with a capital letter followed by lowercase letters")
        private String firstName;

        @NotNull(message = "Last name cannot be null")
        @Pattern(regexp = "[A-Z][a-z]+", message = "Last name must start with a capital letter followed by lowercase letters")
        private String lastName;

        @NotNull(message = "Birth date cannot be empty")
        @PastOrPresent(message = "Birth date must be a past date")
        private LocalDate birthDate;

        private String aboutMe;
    }


