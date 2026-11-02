package com.talk_space.api.controller.publics;
import com.talk_space.connections.ActorService;
import com.talk_space.search.UserSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.*;
@RestController @RequestMapping("/api/public/search") @RequiredArgsConstructor @Validated
public class SearchController {
    private final UserSearchService search;
    private final ActorService actors;
    @GetMapping("/users") public UserSearchService.Results users(
        @RequestParam @NotBlank @Size(max=100) String q,
        @RequestParam(defaultValue="0") @Min(0) @Max(99) int page,
        @RequestParam(defaultValue="20") @Min(1) @Max(100) int size) {
        return search.find(actors.current().getUserName(), q.trim(), page, size);
    }
}
