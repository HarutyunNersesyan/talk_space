package com.talk_space.validation;

import java.util.regex.*;

public class PasswordValidator {

    public static boolean isValidPassword(String password) {
        // Regex to check for at least one uppercase, one lowercase, one digit, and one special character
        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/]).{8,}$";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(password);

        return matcher.matches();
    }

}

