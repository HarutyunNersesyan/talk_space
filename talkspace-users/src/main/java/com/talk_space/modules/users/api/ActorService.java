package com.talk_space.modules.users.api;
import com.talk_space.modules.users.api.model.Role;
import com.talk_space.modules.users.api.model.User;
import com.talk_space.modules.users.api.model.Status;
import com.talk_space.modules.users.internal.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.*;
@Service @RequiredArgsConstructor
public class ActorService {
    private final UserRepository users;
    public User currentMember() {
        User actor=current();
        if (actor.getRole()!=com.talk_space.modules.users.api.model.Role.USER) throw new ResponseStatusException(FORBIDDEN);
        return actor;
    }
    public User current() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) throw new ResponseStatusException(UNAUTHORIZED);
        User user = users.findUserByEmail(auth.getName()).orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED));
        if (user.getStatus() != Status.ACTIVE || !Boolean.TRUE.equals(user.getVerifyMail()))
            throw new ResponseStatusException(FORBIDDEN);
        return user;
    }
}
