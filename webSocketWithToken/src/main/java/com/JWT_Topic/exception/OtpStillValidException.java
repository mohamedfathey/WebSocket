package com.JWT_Topic.exception;

public class OtpStillValidException extends RuntimeException {
    public OtpStillValidException(String message) {
        super(message);
    }
}