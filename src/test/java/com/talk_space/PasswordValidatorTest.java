package com.talk_space;

import com.talk_space.validation.PasswordValidator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PasswordValidatorTest {

    @Test
    void testValidPassword() {
        assertTrue(PasswordValidator.isValidPassword("Abcdef1!"));
        assertTrue(PasswordValidator.isValidPassword("Str0ng@Password2024"));
    }

    @Test
    void testTooShortPassword() {
        assertFalse(PasswordValidator.isValidPassword("Ab1!"));
    }

    @Test
    void testNoUppercase() {
        assertFalse(PasswordValidator.isValidPassword("abcdef1!"));
    }

    @Test
    void testNoLowercase() {
        assertFalse(PasswordValidator.isValidPassword("ABCDEF1!"));
    }

    @Test
    void testNoDigit() {
        assertFalse(PasswordValidator.isValidPassword("Abcdefg!"));
    }

    @Test
    void testNoSpecialCharacter() {
        assertFalse(PasswordValidator.isValidPassword("Abcdef12"));
    }

    @Test
    void testNullPassword() {
        assertThrows(NullPointerException.class, () -> PasswordValidator.isValidPassword(null));
    }
}
