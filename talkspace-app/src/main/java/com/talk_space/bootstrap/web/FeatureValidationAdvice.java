package com.talk_space.bootstrap.web;
import com.talk_space.modules.network.internal.web.ConnectionController;
import com.talk_space.modules.discovery.internal.web.SearchController;
import com.talk_space.modules.presence.internal.web.PresenceController;
import com.talk_space.modules.notifications.internal.web.NotificationController;

import com.talk_space.shared.web.ApiError;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.Instant;

@RestControllerAdvice(assignableTypes={ConnectionController.class,NotificationController.class,SearchController.class,PresenceController.class})
public class FeatureValidationAdvice {
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> validation(ConstraintViolationException exception) {
        return ResponseEntity.badRequest().body(new ApiError(400,"INVALID_REQUEST","Invalid request parameters",Instant.now()));
    }
}
