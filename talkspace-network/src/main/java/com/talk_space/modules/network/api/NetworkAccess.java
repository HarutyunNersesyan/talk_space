package com.talk_space.modules.network.api;
import com.talk_space.modules.users.api.model.Role;
import com.talk_space.modules.users.api.model.Status;
import com.talk_space.modules.network.internal.persistence.ConnectionRepository;
import com.talk_space.modules.users.api.ActorService;

import com.talk_space.modules.network.internal.persistence.UserBlockRepository;
import com.talk_space.modules.users.api.model.User;
import com.talk_space.modules.users.api.UserDirectory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import static org.springframework.http.HttpStatus.*;

@Service @RequiredArgsConstructor
public class NetworkAccess {
    private final UserDirectory users;
    private final UserBlockRepository blocks;
    private final ConnectionRepository connections;
    private final ActorService actors;

    public java.util.List<User> partners(String actor) { return users.findActiveMembers(connections.findConnectionPartnerNames(actor)); }
    public boolean blockedEitherWay(String actor,String target) { return blocks.blockedEitherWay(actor,target); }
    public java.util.Set<String> hidden(String actor) {
        var hidden=new java.util.HashSet<String>();hidden.add(actor);
        for (var b:blocks.involving(actor)) hidden.add(b.getBlocker().equals(actor)?b.getBlocked():b.getBlocker());
        return hidden;
    }
    public List<String> pair(String a, String b) { var pair=new ArrayList<>(List.of(a,b)); Collections.sort(pair); return pair; }
    // Connection, blocking and message writes share the same lock order.
    public void lockPair(String a, String b) {
        for (String name:pair(a,b)) users.lockMatchingViewer(name).orElseThrow(()->new ResponseStatusException(NOT_FOUND));
    }
    public User member(String name) {
        return users.findUserByUserName(name).filter(u->u.getRole()==Role.USER && u.getStatus()==Status.ACTIVE && Boolean.TRUE.equals(u.getVerifyMail()))
            .orElseThrow(()->new ResponseStatusException(NOT_FOUND, "Profile is unavailable"));
    }
    public void requireVisible(String actor, String target) {
        if (!actor.equals(target) && blocks.blockedEitherWay(actor,target)) throw new ResponseStatusException(NOT_FOUND,"Profile is unavailable");
    }
    public void requireCurrentVisible(String target) {
        var actor=actors.current();
        if (actor.getRole()!=Role.ADMIN) { requireVisible(actor.getUserName(),target); member(target); }
    }
    public void requireAdmin() {
        if (actors.current().getRole() != Role.ADMIN) throw new ResponseStatusException(FORBIDDEN);
    }
    public void requireOwnUserId(Long id) {
        var actor = actors.current();
        if (actor.getRole() != Role.ADMIN && !java.util.Objects.equals(actor.getUserId(), id))
            throw new ResponseStatusException(FORBIDDEN);
    }
    public void requireOwnEmail(String email) {
        var actor=actors.current();
        if (actor.getRole()!=Role.ADMIN && !actor.getEmail().equalsIgnoreCase(email)) throw new ResponseStatusException(FORBIDDEN);
    }
    public void requireChat(String a, String b) {
        if (a.equals(b)) throw new ResponseStatusException(FORBIDDEN,"Accept a connection before chatting");
        member(a); member(b); requireVisible(a,b);
        var pair=pair(a,b);
        if (connections.findByPairLowAndPairHigh(pair.get(0),pair.get(1)).filter(c->c.getStatus()==Connection.State.ACCEPTED).isEmpty())
            throw new ResponseStatusException(FORBIDDEN,"Accept a connection before chatting");
    }
    public void requireRestActor(String name) {
        if (!actors.currentMember().getUserName().equals(name)) throw new ResponseStatusException(FORBIDDEN);
    }
}
