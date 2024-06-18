package ru.practicum.exceptiion;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.controller.*;

@Slf4j
@RestControllerAdvice(assignableTypes = {CategoryAdminController.class, CategoryPublicController.class,
        CompilationAdminController.class, CompilationPublicController.class, EventAdminController.class,
        EventPrivateController.class, EventPublicController.class, ParticipationPrivateController.class,
        UserController.class})
public class HandlerException {

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> badRequest(final BadRequestException e) {
        log.warn("400 {}", e.getMessage());
        return new ResponseEntity<>(ErrorResponse.builder()
                .error("Bad request")
                .message(e.getMessage())
                .build(), e.getHttpStatus());
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> conflict(final ConflictException e) {
        log.warn("409 {}", e.getMessage());
        return new ResponseEntity<>(ErrorResponse.builder()
                .error("Conflict exception")
                .message(e.getMessage())
                .build(), e.getHttpStatus());
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> notFound(final NotFoundException e) {
        log.warn("404 {}", e.getMessage());
        return new ResponseEntity<>(ErrorResponse.builder()
                .error("not found")
                .message(e.getMessage())
                .build(), e.getHttpStatus());
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> methodArgumentNotValid(final MethodArgumentNotValidException e) {
        log.warn("400 {}", e.getMessage());
        return new ResponseEntity<>(ErrorResponse.builder()
                .error(e.getMessage())
                .build(), HttpStatus.BAD_REQUEST);
    }
}

