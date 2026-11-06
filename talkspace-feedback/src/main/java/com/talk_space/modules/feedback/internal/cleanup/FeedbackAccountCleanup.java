package com.talk_space.modules.feedback.internal.cleanup;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import com.talk_space.modules.users.api.UserDeleting;
import com.talk_space.modules.feedback.internal.persistence.FeedBacksRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
@Component @RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class FeedbackAccountCleanup {
    private final FeedBacksRepository feedback;
    @org.springframework.core.annotation.Order(0)
    @EventListener public void remove(UserDeleting event) { feedback.deleteAll(feedback.findFeedBacks(event.userName())); feedback.flush(); }
}
