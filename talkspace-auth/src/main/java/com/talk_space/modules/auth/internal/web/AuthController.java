package com.talk_space.modules.auth.internal.web;
import com.talk_space.modules.auth.api.AuthService;
import com.talk_space.modules.auth.internal.session.RefreshSessionService;
import com.talk_space.modules.auth.api.dto.UserDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.*;
@RestController @RequestMapping("/account")
public class AuthController {
    private final AuthService auth;
    private final RefreshSessionService sessions;
    @Value("${app.auth.cookie-secure:false}") private boolean secure;
    @Value("${app.auth.frontend-origin:http://localhost:3000}") private String origin;
    public AuthController(AuthService auth,RefreshSessionService sessions) { this.auth=auth;this.sessions=sessions; }
    private String cookie(String value,long seconds) {
        return ResponseCookie.from("talkspace_refresh",value).httpOnly(true).secure(secure).sameSite("Strict").path("/account").maxAge(seconds).build().toString();
    }
    private void sameOrigin(HttpServletRequest request) {
        String source=request.getHeader("Origin");
        if (source!=null && !source.equals(origin)) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Origin not allowed");
    }
    private ResponseEntity<?> response(RefreshSessionService.Issued issued) {
        return ResponseEntity.ok().header(HttpHeaders.CACHE_CONTROL,"no-store")
            .header(HttpHeaders.SET_COOKIE,cookie(issued.refresh(),Math.max(0,Duration.between(Instant.now(),issued.expiresAt()).getSeconds())))
            .body(issued.access());
    }
    @PostMapping("/auth") public ResponseEntity<?> login(@RequestBody UserDto dto, HttpServletRequest request,
        @CookieValue(name="talkspace_refresh",required=false) String previous) {
        sameOrigin(request);
        var result=auth.authenticate(dto);
        if (!result.getStatusCode().is2xxSuccessful()) return result;
        sessions.logout(previous);
        return response(sessions.start(dto.getEmail()));
    }
    @PostMapping("/refresh") public ResponseEntity<?> refresh(@CookieValue(name="talkspace_refresh",required=false) String raw,
        @RequestHeader(name="X-Refresh-Request",required=false) String guard,HttpServletRequest request) {
        sameOrigin(request);
        if (!"true".equals(guard)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        return response(sessions.refresh(raw));
    }
    @PostMapping("/logout") public ResponseEntity<?> logout(@CookieValue(name="talkspace_refresh",required=false) String raw,
        @RequestHeader(name="X-Refresh-Request",required=false) String guard,HttpServletRequest request) {
        sameOrigin(request);
        if (!"true".equals(guard)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        sessions.logout(raw);
        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE,cookie("",0)).header(HttpHeaders.CACHE_CONTROL,"no-store").build();
    }
    // Kept for old clients; new clients use POST /logout to revoke the refresh session.
    @GetMapping("/profile/logout") public ResponseEntity<String> legacyLogout() { return ResponseEntity.ok("Logged out successfully"); }
}
