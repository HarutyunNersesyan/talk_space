package com.talk_space;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FeedBackServiceTest {

    @Mock
    private FeedBacksRepository feedBacksRepository;

    @InjectMocks
    private FeedBackService feedBackService;

    @Test
    void testAddReview_FirstTime() {
        when(feedBacksRepository.findFeedBacks("user")).thenReturn(Collections.emptyList());

        Object result = feedBackService.addReview("user", "Nice", 4.0);

        assertInstanceOf(FeedBacks.class, result);
        FeedBacks feedback = (FeedBacks) result;
        assertEquals("user", feedback.getUserName());
        assertEquals("Nice", feedback.getMessage());
        assertEquals(4.0, feedback.getRating());

        verify(feedBacksRepository, times(1)).save(any(FeedBacks.class));
    }

    @Test
    void testAddReview_TooSoon() {
        LocalDate lastDate = LocalDate.now().minusDays(1);
        FeedBacks last = new FeedBacks("user", "Old", lastDate, 5.0);
        when(feedBacksRepository.findFeedBacks("user")).thenReturn(List.of(last));

        Object result = feedBackService.addReview("user", "Too soon", 3.0);

        assertInstanceOf(LocalDate.class, result);
        assertEquals(lastDate.plusDays(2), result);

        verify(feedBacksRepository, never()).save(any());
    }

    @Test
    void testAddReview_ZeroRating() {
        when(feedBacksRepository.findFeedBacks("user")).thenReturn(Collections.emptyList());

        Object result = feedBackService.addReview("user", "Zero", 0.0);

        FeedBacks feedback = (FeedBacks) result;
        assertNull(feedback.getRating());
        assertEquals("Zero", feedback.getMessage());

        verify(feedBacksRepository, times(1)).save(any());
    }



    interface FeedBacksRepository {
        List<FeedBacks> findFeedBacks(String userName);
        void save(FeedBacks feedback);
    }

    static class FeedBacks {
        private final String userName;
        private final String message;
        private final LocalDate reviewDate;
        private final Double rating;

        public FeedBacks(String userName, String message, LocalDate reviewDate, Double rating) {
            this.userName = userName;
            this.message = message;
            this.reviewDate = reviewDate;
            this.rating = rating;
        }

        public String getUserName() {
            return userName;
        }

        public String getMessage() {
            return message;
        }

        public LocalDate getReviewDate() {
            return reviewDate;
        }

        public Double getRating() {
            return rating;
        }
    }

    static class FeedBackService {
        private final FeedBacksRepository feedBacksRepository;

        public FeedBackService(FeedBacksRepository feedBacksRepository) {
            this.feedBacksRepository = feedBacksRepository;
        }

        public Object addReview(String userName, String message, Double rating) {
            List<FeedBacks> feedBacks = feedBacksRepository.findFeedBacks(userName);

            if (!feedBacks.isEmpty()) {
                FeedBacks lastFeedback = feedBacks.get(feedBacks.size() - 1);
                LocalDate lastReviewDate = lastFeedback.getReviewDate();
                LocalDate nextAllowedDate = lastReviewDate.plusDays(2);

                if (LocalDate.now().isBefore(nextAllowedDate)) {
                    return nextAllowedDate;
                }
            }

            if (rating == 0){
                rating = null;
            }

            FeedBacks newFeedback = new FeedBacks(userName, message, LocalDate.now(), rating);
            feedBacksRepository.save(newFeedback);
            return newFeedback;
        }
    }
}
