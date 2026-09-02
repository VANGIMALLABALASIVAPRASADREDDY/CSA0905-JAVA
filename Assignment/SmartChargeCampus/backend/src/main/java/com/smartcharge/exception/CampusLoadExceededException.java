package com.smartcharge.exception;

public class CampusLoadExceededException extends RuntimeException {
    public CampusLoadExceededException(String message) {
        super(message);
    }
}
