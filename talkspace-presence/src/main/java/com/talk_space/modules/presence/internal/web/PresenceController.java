package com.talk_space.modules.presence.internal.web;
import com.talk_space.modules.network.api.NetworkAccess;
import com.talk_space.modules.presence.api.PresenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import java.security.Principal;
import java.util.Map;
@Controller @RequiredArgsConstructor
public class PresenceController {
    private final PresenceService presence;
    private final com.talk_space.modules.network.api.NetworkAccess access;
    @MessageMapping("/presence.heartbeat")
    public void heartbeat(Principal principal, @Header("simpSessionId") String session) {
        if (principal != null) presence.heartbeat(principal.getName(), session);
    }
    @GetMapping("/api/public/presence/{userName}") @ResponseBody
    public Map<String, Boolean> online(@PathVariable String userName) { access.requireCurrentVisible(userName); return Map.of("online", presence.online(userName)); }
}
