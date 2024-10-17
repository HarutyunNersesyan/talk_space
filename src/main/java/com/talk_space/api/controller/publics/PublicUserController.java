package com.talk_space.api.controller.publics;

import com.talk_space.model.domain.Chat;
import com.talk_space.model.domain.Image;
import com.talk_space.model.domain.Like;
import com.talk_space.model.domain.User;
import com.talk_space.model.dto.*;
import com.talk_space.model.enums.Role;
import com.talk_space.service.ChatService;
import com.talk_space.service.ImageService;
import com.talk_space.service.LikeService;
import com.talk_space.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public/user")
public class PublicUserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id)));
    }

    @GetMapping("/")
    public ResponseEntity<List<User>> getAll() {
        List<User> users = userService.getAll();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }



}


