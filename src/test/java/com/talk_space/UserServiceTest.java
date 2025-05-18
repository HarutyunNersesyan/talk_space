package com.talk_space;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.talk_space.model.domain.User;
import com.talk_space.model.dto.EditUser;
import com.talk_space.model.enums.Role;
import com.talk_space.model.enums.Status;
import com.talk_space.model.enums.Zodiac;
import com.talk_space.repository.UserRepository;
import com.talk_space.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    @Test
    void testGetUsersWithPaginationAndSorting() {
        User user1 = new User();
        user1.setUserName("user1");
        User user2 = new User();
        user2.setUserName("user2");

        Pageable pageable = PageRequest.of(0, 2, Sort.by("userName"));
        Page<User> page = new PageImpl<>(List.of(user1, user2), pageable, 2);

        when(userRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<User> result = userService.getUsersWithPaginationAndSorting(0, 2, "userName");

        assertEquals(2, result.getContent().size());
        assertEquals("user1", result.getContent().get(0).getUserName());
        verify(userRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void testUpdate() {
        User user = new User();
        user.setUserName("updateUser");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.update(user);

        assertEquals("updateUser", result.getUserName());
        verify(userRepository, times(1)).save(user);
    }


    @Test
    void testUpdateUserByEmailUserNotFound() {
        String email = "notfound@example.com";
        EditUser editUser = new EditUser();

        when(userRepository.findUserByEmail(email)).thenReturn(Optional.empty());

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            userService.updateUserByEmail(email, editUser);
        });

        assertEquals("User with email notfound@example.com not found", thrown.getMessage());
        verify(userRepository, times(1)).findUserByEmail(email);
        verify(userRepository, never()).save(any(User.class));
    }
}