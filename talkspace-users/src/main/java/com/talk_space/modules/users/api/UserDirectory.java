package com.talk_space.modules.users.api;
import com.talk_space.modules.users.api.model.User;
import com.talk_space.modules.users.internal.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
/** Read access to existing user aggregates. Consumers must not mutate returned entities. */
@Service @RequiredArgsConstructor
public class UserDirectory {
    private final UserRepository users;
    @Transactional(readOnly=true) public Optional<User> findUserByEmail(String email) { return users.findUserByEmail(email); }
    @Transactional(readOnly=true) public Optional<User> findUserByUserName(String name) { return users.findUserByUserName(name); }
    /** Caller must keep its transaction open while using this cross-module lock. */
    @Transactional(propagation=org.springframework.transaction.annotation.Propagation.MANDATORY)
    public Optional<User> lockMatchingViewer(String name) { return users.lockMatchingViewer(name); }
    @Transactional(readOnly = true)
    public java.util.List<User> findActiveMembers(java.util.Collection<String> names) {
        if (names.isEmpty()) return java.util.List.of();
        return users.findByUserNameInAndStatusAndRoleAndVerifyMailTrue(names,
                com.talk_space.modules.users.api.model.Status.ACTIVE,
                com.talk_space.modules.users.api.model.Role.USER);
    }
}
