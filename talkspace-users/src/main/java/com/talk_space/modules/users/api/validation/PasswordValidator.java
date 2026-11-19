package com.talk_space.modules.users.api.validation;

public class PasswordValidator {
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 8
            && password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length <= 72
            && password.matches("(?s).*[a-z].*") && password.matches("(?s).*[A-Z].*")
            && password.matches("(?s).*[0-9].*") && password.matches("(?s).*[^A-Za-z0-9\\s].*");
    }
}
