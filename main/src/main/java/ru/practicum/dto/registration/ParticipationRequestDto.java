package ru.practicum.dto.registration;

import lombok.Builder;
import lombok.Data;
import ru.practicum.enums.ParticipationRequestStatus;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
public class ParticipationRequestDto {
    private Long id;
    private Long requester;
    private Long event;
    private LocalDateTime created;
    private ParticipationRequestStatus status;
}