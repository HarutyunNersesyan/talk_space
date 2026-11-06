package com.talk_space.user.service;
import com.talk_space.modules.users.internal.service.UserModerationService;

import com.talk_space.modules.users.api.model.User;
import com.talk_space.modules.users.api.model.Status;
import com.talk_space.modules.users.internal.persistence.UserRepository;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserModerationServiceTest {
    @Test void unblockingActuallyRestoresActiveStatusAndClearsReason() {
        UserRepository users=mock(UserRepository.class); User user=new User();
        user.setStatus(Status.BLOCKED);user.setBlockedMessage("Reason");
        when(users.findUserByUserName("bob")).thenReturn(Optional.of(user));
        new UserModerationService(users).unblockUser("bob");
        assertEquals(Status.ACTIVE,user.getStatus()); assertNull(user.getBlockedMessage());
        verify(users).save(user);
    }
}
