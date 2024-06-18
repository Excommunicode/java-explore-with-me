package ru.practicum.exceptiion;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ConflictException extends RuntimeException {
    private final HttpStatus httpStatus;

    public ConflictException(String message) {
        super(message);
        this.httpStatus = HttpStatus.CONFLICT;
    }
}