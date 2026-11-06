package com.talk_space.shared.web;

import com.talk_space.shared.error.CustomExceptions;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;

@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private ResponseEntity<ApiError> error(int status, String code, String message) {
        return ResponseEntity.status(status).body(new ApiError(status,code,message,Instant.now()));
    }
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> status(ResponseStatusException ex) {
        int code=ex.getStatusCode().value();
        var known=HttpStatus.resolve(code);
        return error(code,known==null ? "REQUEST_ERROR" : known.name(),ex.getReason()==null ? "Request could not be completed" : ex.getReason());
    }
    @ExceptionHandler({MethodArgumentNotValidException.class,ConstraintViolationException.class,IllegalArgumentException.class,
            org.springframework.http.converter.HttpMessageNotReadableException.class,
            org.springframework.web.bind.MissingServletRequestParameterException.class,
            org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiError> invalid(Exception ex) {
        return error(400,"INVALID_REQUEST",ex instanceof IllegalArgumentException ? ex.getMessage() : "Invalid request parameters");
    }
    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> methodNotAllowed(Exception ex) { return error(405,"METHOD_NOT_ALLOWED","Method not allowed"); }
    @ExceptionHandler({org.springframework.web.servlet.resource.NoResourceFoundException.class,
            org.springframework.web.servlet.NoHandlerFoundException.class})
    public ResponseEntity<ApiError> notFound(Exception ex) { return error(404,"NOT_FOUND","Resource not found"); }
    @ExceptionHandler(org.springframework.web.HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiError> unsupported(Exception ex) { return error(415,"UNSUPPORTED_MEDIA_TYPE","Unsupported content type"); }
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ApiError> denied(Exception ex) { return error(403,"FORBIDDEN","Access denied"); }
    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<ApiError> unauthenticated(Exception ex) { return error(401,"UNAUTHORIZED","Authentication required"); }
    @ExceptionHandler(CustomExceptions.UserNotFoundException.class)
    public ResponseEntity<ApiError> missing(CustomExceptions.UserNotFoundException ex) {
        return error(404,"USER_NOT_FOUND",ex.getMessage());
    }
    // The response can no longer be written after a client disconnects.
    @ExceptionHandler(org.springframework.web.context.request.async.AsyncRequestNotUsableException.class)
    public void disconnected(org.springframework.web.context.request.async.AsyncRequestNotUsableException ex) {
        log.debug("Client disconnected while receiving a response: {}", ex.getMessage());
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> unexpected(Exception ex) {
        log.error("Unhandled API exception",ex);
        return error(500,"INTERNAL_ERROR","An unexpected error occurred");
    }
}
