package com.talk_space.matching;
import com.talk_space.repository.UserRepository;
import com.talk_space.model.domain.*;
import com.talk_space.model.enums.Status;
import com.talk_space.exceptions.CustomExceptions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.*;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static com.talk_space.matching.SimilarityCalculator.*;
@ExtendWith(MockitoExtension.class)
class MatchingServiceTest {
    @Mock UserRepository users; @Mock MatchingRepository repository;
    MatchingService service; MatchingProperties props=new MatchingProperties();
    @BeforeEach void setup() { service=new MatchingService(users,repository,new SimilarityCalculator(props),props); }
    User user(String name) {
        User u=new User();u.setUserName(name);u.setStatus(Status.ACTIVE);u.setBirthDate(LocalDate.of(2000,1,1));
        u.setHobbies(List.of(new Hobby("Music",null)));u.setSpecialities(List.of());u.setSocialNetworks(List.of());return u;
    }
    @Test void choosesBestScoreInsteadOfDatabaseOrderAndRecordsIt() {
        when(users.lockMatchingViewer("me")).thenReturn(Optional.of(user("me")));
        when(repository.candidates(eq("me"),eq(MatchMode.HOBBIES),any(),eq(500))).thenReturn(List.of("partial","best"));
        var mine=new Profile(List.of(new Trait(1,10L)),List.of());
        when(repository.profile("me")).thenReturn(mine);
        when(repository.profiles(any())).thenReturn(Map.of("partial",new Profile(List.of(new Trait(2,10L)),List.of()),"best",mine));
        when(users.findUserByUserName("best")).thenReturn(Optional.of(user("best")));
        var result=service.next("me",MatchMode.HOBBIES);
        assertEquals("best",result.getUserName());
        assertEquals(100,result.getMatching().score());
        assertTrue(result.getMatching().exactPrimaryMatch());
        assertNull(result.getMatching().specialityScore());
        assertEquals(100,result.getMatching().hobbyWeight());
        verify(repository).record(eq("me"),eq("best"),eq(MatchMode.HOBBIES),any());
    }
    @Test void seenProfilesReturnInANewCycleWhenNoFreshCandidatesRemain() {
        when(users.lockMatchingViewer("me")).thenReturn(Optional.of(user("me")));
        when(repository.candidates(anyString(),any(),any(),anyInt())).thenReturn(List.of(),List.of("pending"));
        var profile=new Profile(List.of(new Trait(1,10L)),List.of());
        when(repository.profile("me")).thenReturn(profile);when(repository.profiles(any())).thenReturn(Map.of("pending",profile));
        when(users.findUserByUserName("pending")).thenReturn(Optional.of(user("pending")));
        assertEquals("pending",service.next("me",MatchMode.HOBBIES).getUserName());
        verify(repository).restartCycle("me",MatchMode.HOBBIES);
        verify(repository).record(eq("me"),eq("pending"),eq(MatchMode.HOBBIES),any());
    }
    @Test void exhaustedCandidatesPreserveLegacyException() {
        when(users.lockMatchingViewer("me")).thenReturn(Optional.of(user("me")));
        when(repository.candidates(anyString(),any(),any(),anyInt())).thenReturn(List.of());
        assertThrows(CustomExceptions.UserNotFoundException.class,()->service.next("me",MatchMode.HOBBIES));
        verify(repository,never()).record(any(),any(),any(),any());
    }
    @Test void blockedViewerIsRejectedBeforeCandidateQuery() {
        User u=user("me");u.setStatus(Status.BLOCKED);when(users.lockMatchingViewer("me")).thenReturn(Optional.of(u));
        assertThrows(IllegalArgumentException.class,()->service.next("me",MatchMode.HOBBIES));verifyNoInteractions(repository);
    }
    @Test void emptyPrimaryInterestsAreRejected() {
        when(users.lockMatchingViewer("me")).thenReturn(Optional.of(user("me")));
        assertThrows(IllegalArgumentException.class,()->service.next("me",MatchMode.SPECIALITIES));verifyNoInteractions(repository);
    }
}
