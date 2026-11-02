package com.talk_space.api.controller.publics;
import com.talk_space.infrastructure.PresenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import java.security.Principal;
import java.util.Map;
@Controller @RequiredArgsConstructor
public class PresenceController {
    private final PresenceService presence;
    private final com.talk_space.connections.NetworkAccess access;
    @MessageMapping("/presence.heartbeat")
    public void heartbeat(Principal principal, @Header("simpSessionId") String session) {
        if (principal != null) presence.heartbeat(principal.getName(), session);
    }
    @GetMapping("/api/public/presence/{userName}") @ResponseBody
    public Map<String, Boolean> online(@PathVariable String userName) { access.requireCurrentVisible(userName); return Map.of("online", presence.online(userName)); }
}
