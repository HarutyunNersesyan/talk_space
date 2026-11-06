package com.talk_space.modules.discovery.internal.web;
import com.talk_space.modules.discovery.api.MatchMode;
import com.talk_space.modules.discovery.api.MatchingService;

import com.talk_space.modules.users.api.ActorService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

@RestController @RequiredArgsConstructor @RequestMapping("/api/public/matching")
public class MatchingController {
    private final ActorService actors;
    private final MatchingService matching;
    public record SkipRequest(@NotBlank @Size(max=20) String userName, @NotNull MatchMode mode) {}
    @PostMapping("/skip") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void skip(@Valid @RequestBody SkipRequest request) {
        matching.skip(actors.currentMember().getUserName(),request.userName(),request.mode());
    }
}
