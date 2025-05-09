package com.talk_space.service;


import com.talk_space.model.domain.FeedBacks;
import com.talk_space.repository.FeedBacksRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;


@Service
@RequiredArgsConstructor
public class FeedBacksService {

    private final FeedBacksRepository feedBacksRepository;


    public List<FeedBacks> getAll(String userName) {
        return feedBacksRepository.findFeedBacks(userName);
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


    public List<FeedBacks> getAll() {
        return feedBacksRepository.findAll();
    }


}
