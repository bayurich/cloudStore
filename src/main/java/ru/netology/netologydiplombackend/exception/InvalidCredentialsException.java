package ru.netology.netologydiplombackend.exception;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String msg) {
        super(msg);
    }
}
