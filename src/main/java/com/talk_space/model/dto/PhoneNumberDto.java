package com.talk_space.model.dto;


import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Component
public class PhoneNumberDto {

    private String userName;
    @Pattern(
            regexp = "^(\\+374\\d{8})$|^(\\+7\\d{10})$|^(\\+1\\d{10})$|^(\\+995\\d{9})$",
            message = "Invalid phone number. Please use the correct format for Armenia (+374), Russia (+7), USA (+1), or Georgia (+995)."
    )
    private String phoneNumber;
}
