package com.talk_space.matching;

import com.talk_space.exceptions.CustomExceptions;
import com.talk_space.model.domain.User;
import com.talk_space.model.dto.SearchUser;
import com.talk_space.model.enums.Status;
import com.talk_space.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import static com.talk_space.matching.MatchEvaluation.percent;

@Service @RequiredArgsConstructor
public class MatchingService {
    private final UserRepository users;
    private final MatchingRepository matching;
    private final SimilarityCalculator calculator;
    private final MatchingProperties properties;

    @Transactional
    public SearchUser next(String userName, MatchMode mode) {
        User viewer = users.lockMatchingViewer(userName)
            .orElseThrow(() -> new CustomExceptions.UserNotFoundException("User not found."));
        if (viewer.getStatus()!=Status.ACTIVE) throw new IllegalArgumentException("Account is not active");
        var selected = mode==MatchMode.HOBBIES ? viewer.getHobbies() : viewer.getSpecialities();
        if (selected==null || selected.isEmpty()) throw new IllegalArgumentException(
            mode==MatchMode.HOBBIES ? "User hobbies cannot be empty" : "User specialities cannot be empty");
        Instant now = Instant.now();
        List<String> candidates = matching.candidates(userName,mode,now.minus(properties.getHistoryDays(),ChronoUnit.DAYS),properties.getCandidateLimit());
        if (candidates.isEmpty()) {
            // Cooldowns apply to both passes. Resetting a cycle never erases a recent skip.
            candidates = matching.candidates(userName,mode,now.plusSeconds(1),properties.getCandidateLimit());
            if (candidates.isEmpty()) throw exhausted();
            matching.restartCycle(userName,mode);
        }
        var profiles = matching.profiles(candidates);
        var mine = matching.profile(userName);
        var ranked = candidates.stream().filter(profiles::containsKey)
            .map(name -> new MatchSelector.Candidate(name,calculator.evaluate(mine,profiles.get(name),mode))).toList();
        var chosen = MatchSelector.choose(ranked,properties);
        if (chosen==null) throw exhausted();
        User candidate = users.findUserByUserName(chosen.name()).orElseThrow(MatchingService::exhausted);
        SearchUser response = new SearchUser(candidate);
        var other = profiles.get(chosen.name());
        var evaluation = chosen.evaluation();
        response.setMatching(new MatchEvaluation("2.0",mode.name(),percent(evaluation.score()),
            evaluation.hobbies()==null ? null : percent(evaluation.hobbies()),
            evaluation.specialities()==null ? null : percent(evaluation.specialities()),
            percent(evaluation.hobbyWeight()),percent(evaluation.specialityWeight()),evaluation.primaryExact(),
            matching.names(MatchMode.HOBBIES,calculator.commonIds(mine.hobbies(),other.hobbies())),
            matching.names(MatchMode.SPECIALITIES,calculator.commonIds(mine.specialities(),other.specialities())),
            matching.names(MatchMode.HOBBIES,calculator.commonCategories(mine.hobbies(),other.hobbies())),
            matching.names(MatchMode.SPECIALITIES,calculator.commonCategories(mine.specialities(),other.specialities()))));
        matching.record(userName,chosen.name(),mode,now);
        return response;
    }
    @Transactional
    public void skip(String viewer, String candidate, MatchMode mode) {
        users.lockMatchingViewer(viewer).orElseThrow(() -> new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND));
        if (viewer.equals(candidate) || !matching.skip(viewer,candidate,mode,Instant.now()))
            throw new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND,"No shown profile found for this search mode");
    }
    private static CustomExceptions.UserNotFoundException exhausted() {
        return new CustomExceptions.UserNotFoundException("No available matches right now. Skipped profiles and recent requests have a temporary cooldown. Try again later or update your interests.");
    }
}
