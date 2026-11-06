package com.talk_space.seed;
import com.talk_space.modules.users.api.model.Hobby;
import com.talk_space.modules.users.api.model.Location;
import com.talk_space.modules.users.api.model.Speciality;
import com.talk_space.modules.users.api.model.User;
import com.talk_space.modules.users.internal.persistence.SpecialityRepository;
import com.talk_space.modules.users.internal.persistence.LocationRepository;
import com.talk_space.modules.users.internal.persistence.HobbyRepository;
import com.talk_space.modules.users.internal.persistence.UserRepository;
import com.talk_space.bootstrap.seed.DemoProfileSeeder;
import com.talk_space.bootstrap.seed.ProfileSeedMarker;
import com.talk_space.bootstrap.seed.ProfileSeedMarkerRepository;

import com.talk_space.shared.infrastructure.RedisSupport;
import com.talk_space.modules.users.api.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DemoProfileSeederTest {
    @Mock UserRepository users;
    @Mock HobbyRepository hobbies;
    @Mock SpecialityRepository specialities;
    @Mock LocationRepository locations;
    @Mock ProfileSeedMarkerRepository markers;
    @Mock RedisSupport redis;
    DemoProfileSeeder seeder;
    @BeforeEach void setup() { seeder = new DemoProfileSeeder(users,hobbies,specialities,locations,markers,redis); }
    User aram() {
        User user = new User(); user.setUserName("AramH95"); user.setEmail("aram.hakob95@gmail.com");
        user.setRole(Role.USER); user.setVerifyMail(true); user.setHobbies(new ArrayList<>());
        user.setSpecialities(new ArrayList<>()); return user;
    }
    @Test void enrichesOnlyMissingProfileFieldsForKnownUser() {
        User user = aram();
        when(users.findUserByUserName("AramH95")).thenReturn(Optional.of(user));
        when(hobbies.findHobbyByName(anyString())).thenAnswer(inv -> Optional.of(new Hobby(inv.getArgument(0),null)));
        when(specialities.findSpecialityByName(anyString())).thenAnswer(inv -> Optional.of(new Speciality(inv.getArgument(0),null)));
        seeder.run(null);
        assertNotNull(user.getAboutMe()); assertFalse(user.getHobbies().isEmpty());
        assertFalse(user.getSpecialities().isEmpty()); assertEquals("Yerevan",user.getLocation().getCity());
        assertSame(user,user.getLocation().getUser()); verify(markers).save(any(ProfileSeedMarker.class));
        verify(redis).evict("matching:AramH95");
    }
    @Test void preservesExistingProfileDataAndLocation() {
        User user = aram(); user.setAboutMe("My own bio");
        user.setHobbies(List.of(new Hobby("My hobby",null)));
        user.setSpecialities(List.of(new Speciality("My profession",null)));
        Location existing = new Location(); existing.setCity("My city"); user.setLocation(existing);
        when(users.findUserByUserName("AramH95")).thenReturn(Optional.of(user));
        when(locations.findByUserUserName("AramH95")).thenReturn(Optional.of(existing));
        seeder.run(null);
        assertEquals("My own bio",user.getAboutMe()); assertEquals("My hobby",user.getHobbies().get(0).getName());
        assertEquals("My profession",user.getSpecialities().get(0).getName()); assertSame(existing,user.getLocation());
        verifyNoInteractions(hobbies,specialities,redis);
    }
    @Test void doesNotAlterUnrelatedAccountWithSameUsername() {
        User user = aram(); user.setEmail("another.user@gmail.com");
        when(users.findUserByUserName("AramH95")).thenReturn(Optional.of(user));
        seeder.run(null);
        assertNull(user.getAboutMe()); assertNull(user.getLocation());
        verify(users,never()).save(any()); verify(markers,never()).save(any());
    }
    @Test void markedUserIsNeverReseededAfterFieldsAreCleared() {
        when(markers.existsById("AramH95")).thenReturn(true);
        seeder.run(null);
        verify(users,never()).findUserByUserName("AramH95");
    }
}
