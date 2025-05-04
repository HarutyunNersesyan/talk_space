package com.talk_space.api.controller;

import com.talk_space.api.security.JwtTokenUtils;
import com.talk_space.model.domain.User;
import com.talk_space.model.dto.TokenResponse;
import com.talk_space.model.dto.UserDto;
import com.talk_space.model.enums.Status;
import com.talk_space.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/account")
public class AuthController {

    private final UserService userService;
    private final JwtTokenUtils jwtTokenUtils;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthController(UserService userService, JwtTokenUtils jwtTokenUtils, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.jwtTokenUtils = jwtTokenUtils;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/auth")
    public ResponseEntity<?> createAuthToken(@RequestBody UserDto userDto) {
        try {
            User user = userService.findUserByEmail(userDto.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (user.getStatus() != Status.ACTIVE) {
                StringBuilder message = new StringBuilder();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                if (user.getUntilBlockedDate() != null) {
                    message.append("Your account is blocked until ")
                            .append(user.getUntilBlockedDate().format(formatter))
                            .append(":\n");
                }

                if (user.getBlockedMessage() != null && !user.getBlockedMessage().isEmpty()) {
                    message.append("Blocked message: ")
                            .append(user.getBlockedMessage());
                }

                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(message.toString());
            }

            UserDetails userDetails = userService.loadUserByUsername(userDto.getEmail());
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    userDetails.getUsername(),
                    userDto.getPassword()
            ));
            String token = jwtTokenUtils.generateToken(userDetails);
            return ResponseEntity.ok(new TokenResponse(200, token, user.getUserName()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Wrong email or password");
        }
    }

    @GetMapping("/profile/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        SecurityContextHolder.clearContext();
        return new ResponseEntity<>("Logged out successfully", HttpStatus.OK);
    }
}