package com.koubek.exceptions;

public class CameraNotInitializedException extends RuntimeException {

    public CameraNotInitializedException(String message) {
        super(message);
    }
}
