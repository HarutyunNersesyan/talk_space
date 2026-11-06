package com.talk_space.modules.users.api.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@AllArgsConstructor
@Data
public class DeleteAccount {

    private String email;


    private String password;
}
