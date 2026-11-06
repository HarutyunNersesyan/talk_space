package com.talk_space.modules.users.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class BlockAccount {

    private String userName;
    private String blockMessage;
    private LocalDate blockUntil;
}
