package ru.practicum.exceptiion;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.controller.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestControllerAdvice(assignableTypes = {CategoryAdminController.class, CategoryPublicController.class,
        CompilationAdminController.class, CompilationPublicController.class, EventAdminController.class,
        EventPrivateController.class, EventPublicController.class, ParticipationPrivateController.class,
        UserAdminController.class})
public class HandlerException {


    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler
    public ResponseEntity<Object> badRequest(final BadRequestException e) {
        List<String> errors = new ArrayList<>();

        log.error("400 {}", e.getMessage());
        ApiError apiError = ApiError.builder()
                .errors(errors)
                .status(HttpStatus.BAD_REQUEST)
                .message(e.getMessage())
                .reason("The required object was not found")
                .build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler
    public ResponseEntity<Object> conflict(final ConflictException e) {
        List<String> errors = new ArrayList<>();
        for (StackTraceElement stackTraceElement : e.getStackTrace()) {
            errors.add(stackTraceElement.getClassName() + " " +
                    stackTraceElement.getModuleName() + ": " +
                    stackTraceElement.getMethodName() + " row " +
                    stackTraceElement.getLineNumber());

        }
        log.error("409 {}", e.getMessage());
        ApiError apiError = ApiError.builder()
                .errors(errors)
                .status(HttpStatus.CONFLICT)
                .message(e.getMessage())
                .reason("The required object was not found")
                .build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler
    public ResponseEntity<Object> notFound(final NotFoundException e) {
        List<String> errors = new ArrayList<>();
        for (StackTraceElement stackTraceElement : e.getStackTrace()) {
            errors.add(stackTraceElement.getClassName() + " " +
                    stackTraceElement.getModuleName() + ": " +
                    stackTraceElement.getMethodName() + " row " +
                    stackTraceElement.getLineNumber());

        }
        log.error("400 {}", e.getMessage());
        ApiError apiError = ApiError.builder()
                .errors(errors)
                .status(HttpStatus.NOT_FOUND)
                .message(e.getMessage())
                .reason("The required object was not found")
                .build();
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> methodArgumentNotValid(final MethodArgumentNotValidException e) {
        log.error("400 {}", e.getMessage());
        return new ResponseEntity<>(ErrorResponse.builder()
                .error(e.getMessage())
                .build(), HttpStatus.BAD_REQUEST);
    }
}

