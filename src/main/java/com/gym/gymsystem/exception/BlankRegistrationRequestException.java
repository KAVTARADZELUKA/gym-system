package com.gym.gymsystem.exception;

public class BlankRegistrationRequestException extends RuntimeException {
    public BlankRegistrationRequestException(String message) {
        super(message);
    }
}
