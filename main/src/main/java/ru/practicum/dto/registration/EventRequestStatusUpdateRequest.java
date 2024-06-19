package ru.practicum.dto.registration;

import lombok.Data;
import ru.practicum.enums.ParticipationRequestStatus;

import java.util.List;

@Data
public class EventRequestStatusUpdateRequest {
    private List<Long> requestIds;
    private ParticipationRequestStatus status;
}
