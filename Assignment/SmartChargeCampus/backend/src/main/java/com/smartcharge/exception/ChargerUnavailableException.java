package com.smartcharge.exception;

public class ChargerUnavailableException extends RuntimeException {
    public ChargerUnavailableException(String message) {
        super(message);
    }
}
