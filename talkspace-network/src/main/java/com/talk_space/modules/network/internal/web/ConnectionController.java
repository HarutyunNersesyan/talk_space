package com.talk_space.modules.network.internal.web;
import com.talk_space.modules.users.api.dto.SearchUser;
import com.talk_space.modules.network.api.Connection;
import com.talk_space.modules.network.api.NetworkAccess;
import com.talk_space.modules.users.api.ActorService;
import com.talk_space.modules.network.api.ConnectionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import java.util.Map;
@RestController @RequestMapping("/api/public/connections") @RequiredArgsConstructor @Validated
public class ConnectionController {
    private final ConnectionService connections;
    private final ActorService actors;
    private final com.talk_space.modules.network.api.NetworkAccess access;
    public record View(Long id, String sender, String receiver, Connection.State status,
        java.time.Instant createdAt, java.time.Instant updatedAt, com.talk_space.modules.users.api.dto.SearchUser profile) {}
    private View view(Connection c, String actor) {
        String target=actor.equals(c.getSender())?c.getReceiver():c.getSender();
        com.talk_space.modules.users.api.dto.SearchUser profile=null;
        try { access.requireVisible(actor,target); profile=new com.talk_space.modules.users.api.dto.SearchUser(access.member(target)); }
        catch (org.springframework.web.server.ResponseStatusException ignored) {}
        return new View(c.getId(),c.getSender(),c.getReceiver(),c.getStatus(),c.getCreatedAt(),c.getUpdatedAt(),profile);
    }
    public record Request(@NotBlank @Size(max=20) String userName) {}
    @PostMapping @org.springframework.transaction.annotation.Transactional public View request(@Valid @RequestBody Request body) {
        String actor=actors.currentMember().getUserName();
        return view(connections.request(actor, body.userName()),actor);
    }
    @PostMapping("/{id}/{action}") @org.springframework.transaction.annotation.Transactional public View action(@PathVariable long id, @PathVariable String action) {
        String actor=actors.currentMember().getUserName();
        return view(connections.transition(actor, id, action),actor);
    }
    @GetMapping @org.springframework.transaction.annotation.Transactional(readOnly=true) public Page<View> list(@RequestParam(defaultValue="0") @Min(0) int page,
        @RequestParam(defaultValue="20") @Min(1) @Max(100) int size) {
        String actor=actors.currentMember().getUserName();
        return connections.list(actor, page, size).map(c->view(c,actor));
    }
    @GetMapping("/pending-count") public Map<String, Long> pending() {
        return Map.of("count", connections.pending(actors.currentMember().getUserName()));
    }
}
