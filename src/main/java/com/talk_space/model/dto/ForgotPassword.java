package com.talk_space.model.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Random;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Component
public class ForgotPassword {


    @Email(message = "Email should be valid")
    private String email;


    @Pattern(regexp = "^[1-9][0-9]{5}$")
    private String pin;

    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+])[a-zA-Z0-9!@#$%^&*()_+]{8,20}$", message = "The password must contain uppercase and lowercase letters, mathematical symbols and numbers.")
    private String newPassword;


    public String generatePin() {
        Random random = new Random();
        return String.valueOf(random.nextInt(100000, 999999));
    }


}
