package com.talk_space.modules.auth.internal.session;
import com.talk_space.modules.auth.internal.security.JwtTokenUtils;
import com.talk_space.modules.users.api.model.User;
import com.talk_space.modules.auth.api.dto.TokenResponse;
import com.talk_space.modules.users.api.model.Status;
import com.talk_space.modules.users.api.UserDirectory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.security.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;
@Service @RequiredArgsConstructor
public class RefreshSessionService {
    private final RefreshSessionRepository sessions;
    private final UserDirectory users;
    private final JwtTokenUtils jwt;
    private static final SecureRandom RANDOM=new SecureRandom();
    public record Issued(String refresh, TokenResponse access, Instant expiresAt) {}
    static String hash(String value) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); }
        catch (NoSuchAlgorithmException ex) { throw new IllegalStateException(ex); }
    }
    private ResponseStatusException invalid() { return new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Session expired. Please sign in again."); }
    private User member(String email) {
        User user=users.findUserByEmail(email).orElseThrow(this::invalid);
        if (user.getStatus()!=Status.ACTIVE || !Boolean.TRUE.equals(user.getVerifyMail())) throw invalid();
        return user;
    }
    private Issued issue(RefreshSession s, User u) {
        byte[] bytes=new byte[32]; RANDOM.nextBytes(bytes);
        String raw=s.getId()+"."+Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        s.setTokenHash(hash(raw)); sessions.save(s);
        var details=org.springframework.security.core.userdetails.User.withUsername(u.getEmail()).password(u.getPassword()).authorities(u.getRole().name()).build();
        return new Issued(raw,new TokenResponse(200,jwt.generateToken(details),u.getUserName()),s.getExpiresAt());
    }
    @Transactional public Issued start(String email) {
        User u=member(email); RefreshSession s=new RefreshSession();
        s.setId(UUID.randomUUID().toString());s.setEmail(u.getEmail());s.setPasswordHash(hash(u.getPassword()));
        s.setExpiresAt(Instant.now().plus(Duration.ofDays(30)));
        return issue(s,u);
    }
    private RefreshSession lookup(String raw) {
        if (raw==null || raw.length()>150 || !raw.contains(".")) throw invalid();
        RefreshSession s=sessions.lock(raw.substring(0,raw.indexOf('.'))).orElseThrow(this::invalid);
        if (s.isRevoked() || !s.getExpiresAt().isAfter(Instant.now()) || !MessageDigest.isEqual(hash(raw).getBytes(StandardCharsets.US_ASCII),s.getTokenHash().getBytes(StandardCharsets.US_ASCII))) throw invalid();
        return s;
    }
    @Transactional public Issued refresh(String raw) {
        RefreshSession s=lookup(raw); User u=member(s.getEmail());
        if (!hash(u.getPassword()).equals(s.getPasswordHash())) throw invalid();
        return issue(s,u);
    }
    @Transactional public void logout(String raw) {
        try { lookup(raw).setRevoked(true); } catch (ResponseStatusException ignored) { /* Idempotent logout. */ }
    }
}
