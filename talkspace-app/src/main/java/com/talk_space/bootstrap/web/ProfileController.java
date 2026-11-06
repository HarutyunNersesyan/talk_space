package com.talk_space.bootstrap.web;
import com.talk_space.modules.network.api.NetworkAccess;
import com.talk_space.modules.users.api.ActorService;

import com.talk_space.modules.users.api.dto.SearchUser;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/public/profiles") @RequiredArgsConstructor
public class ProfileController {
    private final ActorService actors;
    private final NetworkAccess access;
    @GetMapping("/{userName}") @Transactional(readOnly=true)
    public SearchUser profile(@PathVariable String userName) {
        access.requireVisible(actors.currentMember().getUserName(),userName);
        return new SearchUser(access.member(userName));
    }
}
