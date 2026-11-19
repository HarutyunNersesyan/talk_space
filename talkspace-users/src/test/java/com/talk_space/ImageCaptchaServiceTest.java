package com.talk_space;
import com.talk_space.modules.users.api.ImageCaptchaService;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class ImageCaptchaServiceTest {
    private ImageCaptchaService service() {
        var source=org.mockito.Mockito.mock(com.talk_space.modules.users.api.OnlineCaptchaPhotos.class);
        List<com.talk_space.modules.users.api.OnlineCaptchaPhotos.Photo> photos=new ArrayList<>();
        for(int i=0;i<9;i++) photos.add(new com.talk_space.modules.users.api.OnlineCaptchaPhotos.Photo(i/3,"data:image/png;base64,test"+i));
        org.mockito.Mockito.when(source.next()).thenReturn(photos);
        return new ImageCaptchaService(source);
    }
    @Test void unavailableInternetReturnsServiceUnavailableWithoutStartupFailure() {
        var source=org.mockito.Mockito.mock(com.talk_space.modules.users.api.OnlineCaptchaPhotos.class);
        org.mockito.Mockito.when(source.next()).thenThrow(new IllegalStateException("Images unavailable"));
        var service=new ImageCaptchaService(source);
        assertEquals(503,assertThrows(ResponseStatusException.class,service::create).getStatusCode().value());
    }

    @Test void challengeUsesRasterImagesAndIsConsumedOnFailure() {
        ImageCaptchaService service=service(); var c=service.create();
        assertEquals(9,c.images().size());
        assertTrue(c.images().stream().allMatch(s -> s.startsWith("data:image/png;base64,")));
        assertThrows(ResponseStatusException.class,()->service.consume(c.id(),List.of()));
        assertThrows(ResponseStatusException.class,()->service.consume(c.id(),List.of(0,1,2)));
    }
    @Test void missingChallengeCannotBypassVerification() {
        assertThrows(ResponseStatusException.class,()->service().consume(null,List.of(0,1,2)));
    }

    @SuppressWarnings("unchecked")
    private List<Integer> solution(ImageCaptchaService service,String id) {
        Map<String,?> answers=(Map<String,?>)org.springframework.test.util.ReflectionTestUtils.getField(service,"answers");
        Set<Integer> tiles=(Set<Integer>)org.springframework.test.util.ReflectionTestUtils.getField(answers.get(id),"tiles");
        return new ArrayList<>(tiles);
    }
    @Test void correctSelectionMustBeVerifiedBeforeRegistrationAndCannotBeReused() {
        ImageCaptchaService service=service(); var c=service.create();
        assertTrue(c.prompt().matches(".*(traffic lights|cars|bicycles).*"));
        List<Integer> correct=solution(service,c.id());
        assertEquals(3,correct.size());
        assertTrue(service.verify(c.id(),correct).expiresAt().isAfter(java.time.Instant.now()));
        assertDoesNotThrow(()->service.consume(c.id(),correct));
        assertThrows(ResponseStatusException.class,()->service.consume(c.id(),correct));
    }
    @Test void unverifiedAnswerCannotBeUsedForRegistration() {
        ImageCaptchaService service=service(); var c=service.create();
        List<Integer> correct=solution(service,c.id());
        assertThrows(ResponseStatusException.class,()->service.consume(c.id(),correct));
    }
}
