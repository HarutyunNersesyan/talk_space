package com.talk_space.api.controller;


import com.talk_space.api.security.JwtTokenUtils;
import com.talk_space.model.dto.TokenResponse;
import com.talk_space.model.dto.UserDto;
import com.talk_space.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

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
    public TokenResponse createAuthToken(@RequestBody UserDto userDto) {
        UserDetails userDetails = userService.loadUserByUsername(userDto.getEmail());
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userDetails.getUsername(), userDto.getPassword()));
        String token = jwtTokenUtils.generateToken(userDetails);
        return new TokenResponse(200,token);
    }

    @GetMapping("/profile/logout")
    public String logout() {
        return "redirect:/logout";
    }
}
