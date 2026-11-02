package com.talk_space.api.controller.publics;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import jakarta.validation.ConstraintViolationException;
import java.util.Map;
@RestControllerAdvice(assignableTypes={ConnectionController.class,NotificationController.class,SearchController.class,PresenceController.class})
public class FeatureValidationAdvice {
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String,String>> validation(ConstraintViolationException exception) {
        return ResponseEntity.badRequest().body(Map.of("message","Invalid request parameters"));
    }
}
