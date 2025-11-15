package com.example.bankcards.exception;

public class DestinationCardNotFoundException extends RuntimeException {
    public DestinationCardNotFoundException(String message) {
        super(message);
    }
}
