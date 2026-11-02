package com.talk_space.api.controller.publics;
import com.talk_space.connections.ActorService;
import com.talk_space.notifications.*;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import java.util.Map;
@RestController @RequestMapping("/api/public/notifications") @RequiredArgsConstructor @Validated
public class NotificationController {
    private final NotificationService notifications;
    private final ActorService actors;
    @GetMapping public Page<Notification> list(@RequestParam(defaultValue="0") @Min(0) int page,
        @RequestParam(defaultValue="20") @Min(1) @Max(100) int size) {
        return notifications.list(actors.current().getUserName(), page, size);
    }
    @GetMapping("/unread-count") public Map<String, Long> unread() {
        return Map.of("count", notifications.unread(actors.current().getUserName()));
    }
    @PostMapping("/{id}/read") public void read(@PathVariable long id) { notifications.read(actors.current().getUserName(), id); }
    @PostMapping("/read-all") public void readAll() { notifications.readAll(actors.current().getUserName()); }
}
