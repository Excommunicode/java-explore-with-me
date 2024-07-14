package ru.practicum.exceptiion;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class NotFoundException extends RuntimeException {
    private final HttpStatus httpStatus;

    public NotFoundException(String message) {
        super(message);
        httpStatus = HttpStatus.NOT_FOUND;
    }
}