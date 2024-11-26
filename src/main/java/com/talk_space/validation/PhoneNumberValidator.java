package com.talk_space.validation;

import java.util.regex.Pattern;

public class PhoneNumberValidator {

    public static boolean isValidPhoneNumber(String phoneNumber, String country) {
        String regex;
        switch (country.toUpperCase()) {
            case "ARMENIA":
                regex = "^\\+374(10|33|44|55|77|91|93|94|95|96|97|98|99)\\d{6}$";
                break;
            case "USA":
                regex = "^\\+1\\d{10}$";
                break;
            case "RUSSIA":
                regex = "^\\+7\\d{10}$";
                break;
            case "GEORGIA":
                regex = "^\\+995(5\\d{8})$";
                break;
            default:
                throw new IllegalArgumentException("Unsupported country: " + country);
        }
        return Pattern.matches(regex, phoneNumber);
    }

    public static void main(String[] args) {
        System.out.println(isValidPhoneNumber("+37410123456", "Armenia")); // true
        System.out.println(isValidPhoneNumber("+15551234567", "USA")); // true
        System.out.println(isValidPhoneNumber("+79501234567", "Russia")); // true
        System.out.println(isValidPhoneNumber("+995555123456", "Georgia")); // true
        System.out.println(isValidPhoneNumber("+37400123456", "Armenia")); // false
    }
}

