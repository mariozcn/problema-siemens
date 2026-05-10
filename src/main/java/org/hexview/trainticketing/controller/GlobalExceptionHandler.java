package org.hexview.trainticketing.controller;

import org.hexview.trainticketing.dto.ErrorResponse;
import org.hexview.trainticketing.exception.InvalidRequestException;
import org.hexview.trainticketing.exception.NoRouteException;
import org.hexview.trainticketing.exception.NotFoundException;
import org.hexview.trainticketing.exception.OverbookingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> notFound(NotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage());
    }

    @ExceptionHandler(NoRouteException.class)
    public ResponseEntity<ErrorResponse> noRoute(NoRouteException ex) {
        return build(HttpStatus.NOT_FOUND, "No Route", ex.getMessage());
    }

    @ExceptionHandler(OverbookingException.class)
    public ResponseEntity<ErrorResponse> overbooking(OverbookingException ex) {
        return build(HttpStatus.CONFLICT, "Overbooking", ex.getMessage());
    }

    @ExceptionHandler({InvalidRequestException.class, IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> invalid(RuntimeException ex) {
        return build(HttpStatus.BAD_REQUEST, "Invalid Request", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, "Validation Failed", msg);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String error, String message) {
        return ResponseEntity.status(status).body(ErrorResponse.of(status.value(), error, message));
    }
}
