package ru.practicum.exceptiion;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ErrorResponse {
    private String error;
    private String message;
}
