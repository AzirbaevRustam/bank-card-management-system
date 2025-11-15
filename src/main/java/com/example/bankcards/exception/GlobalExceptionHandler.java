package com.example.bankcards.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            CardNotFoundException.class,
            UserNotFoundException.class,
            DestinationCardNotFoundException.class,
            AccessDeniedException.class
    })
    public ResponseEntity<String> handleCustomExceptions(RuntimeException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}