package ru.practicum.exceptiion;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BadRequestException extends RuntimeException {
    private final HttpStatus httpStatus;

    public BadRequestException(String message) {
        super(message);
        httpStatus = HttpStatus.BAD_REQUEST;
    }
}
