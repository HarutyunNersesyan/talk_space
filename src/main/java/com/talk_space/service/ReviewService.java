package com.talk_space.service;


import com.talk_space.model.domain.Review;
import com.talk_space.model.domain.Speciality;
import com.talk_space.repository.ReviewRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;


    public Review addReview(String userName, String message) {
        return reviewRepository.save(new Review(userName, message, LocalDate.now()));
    }

    public List<Review> getAll() {
        return reviewRepository.findAll();
    }



}
