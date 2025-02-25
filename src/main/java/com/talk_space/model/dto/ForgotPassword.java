package com.talk_space.model.dto;


import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Random;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Component
public class ForgotPassword {

    @Email(message = "Email should be valid")
    private String email;


    public String generatePin() {
        Random random = new Random();
        return String.valueOf(random.nextInt(100000, 999999));
    }

    public  String generatePassword() {
        final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
        final String DIGITS = "0123456789";
        final String SYMBOLS = "!@#$%^&*()_+";
        final String ALL_CHARACTERS = UPPERCASE + LOWERCASE + DIGITS + SYMBOLS;
        final int PASSWORD_LENGTH = 12;

        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        password.append(UPPERCASE.charAt(random.nextInt(UPPERCASE.length())));
        password.append(LOWERCASE.charAt(random.nextInt(LOWERCASE.length())));
        password.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
        password.append(SYMBOLS.charAt(random.nextInt(SYMBOLS.length())));

        for (int i = 4; i < PASSWORD_LENGTH; i++) {
            password.append(ALL_CHARACTERS.charAt(random.nextInt(ALL_CHARACTERS.length())));
        }

        return shuffleString(password.toString(), random);
    }

    private static String shuffleString(String input, SecureRandom random) {
        char[] characters = input.toCharArray();
        for (int i = characters.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = characters[i];
            characters[i] = characters[j];
            characters[j] = temp;
        }
        return new String(characters);
    }
}
