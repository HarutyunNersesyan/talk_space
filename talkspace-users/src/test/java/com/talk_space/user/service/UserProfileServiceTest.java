package com.talk_space.user.service;
import com.talk_space.modules.users.internal.service.UserProfileService;

import com.talk_space.modules.users.api.model.User;
import com.talk_space.modules.users.api.dto.EditUser;
import com.talk_space.modules.users.api.model.Zodiac;
import com.talk_space.modules.users.internal.persistence.UserRepository;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserProfileServiceTest {
    @Test void zodiacIsRecalculatedFromTheNewBirthDate() {
        UserRepository users=mock(UserRepository.class);
        User user=new User();user.setUserName("alice");user.setBirthDate(LocalDate.of(1995,1,5));
        when(users.findUserByEmail("alice@gmail.com")).thenReturn(Optional.of(user));
        when(users.save(any(User.class))).thenAnswer(i->i.getArgument(0));
        EditUser data=new EditUser("Alice","Smith",LocalDate.of(2000,7,5),"Hello");
        User updated=new UserProfileService(users).updateUserByEmail("alice@gmail.com",data);
        assertEquals(Zodiac.fromMonthAndDay(7,5),updated.getZodiacSign());
        assertEquals(data.getBirthDate(),updated.getBirthDate());
    }
}
