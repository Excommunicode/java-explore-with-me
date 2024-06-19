package ru.practicum.exceptiion;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.constant.EventConstant.DATE_TIME_FORMATTER;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiError {
    private List<String> errors;
    private String message;
    private String reason;
    private HttpStatus status;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMATTER)
    private LocalDateTime localDateTime = LocalDateTime.now();
}
