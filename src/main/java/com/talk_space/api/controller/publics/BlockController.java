package com.talk_space.api.controller.publics;

import com.talk_space.blocks.*;
import com.talk_space.connections.ActorService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/public/blocks") @RequiredArgsConstructor @Validated
public class BlockController {
    private final BlockService blocks;
    private final ActorService actors;
    public record Request(@NotBlank @Size(max=20) String userName) {}
    @GetMapping public Page<UserBlock> list(@RequestParam(defaultValue="0") @Min(0) int page,
        @RequestParam(defaultValue="20") @Min(1) @Max(100) int size) {
        return blocks.list(actors.currentMember().getUserName(),page,size);
    }
    @PostMapping public UserBlock block(@Valid @RequestBody Request body) { return blocks.block(actors.currentMember().getUserName(),body.userName()); }
    @DeleteMapping("/{userName}") public void unblock(@PathVariable @NotBlank @Size(max=20) String userName) {
        blocks.unblock(actors.currentMember().getUserName(),userName);
    }
}
