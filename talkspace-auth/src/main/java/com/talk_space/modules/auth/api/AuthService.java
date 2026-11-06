package com.talk_space.modules.auth.api;
import com.talk_space.modules.auth.internal.web.AuthController;

import com.talk_space.modules.auth.internal.security.JwtTokenUtils;
import com.talk_space.modules.users.api.model.User;
import com.talk_space.modules.auth.api.dto.TokenResponse;
import com.talk_space.modules.auth.api.dto.UserDto;
import com.talk_space.modules.users.api.model.Status;
import com.talk_space.modules.users.api.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

/** Authentication business logic. Public REST contract stays in AuthController. */
@Service
public class AuthService {
    private final UserService userService;
    private final JwtTokenUtils jwtTokenUtils;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserService userService, JwtTokenUtils jwtTokenUtils,
                       AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.jwtTokenUtils = jwtTokenUtils;
        this.authenticationManager = authenticationManager;
    }

    public ResponseEntity<?> authenticate(UserDto userDto) {
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

}
