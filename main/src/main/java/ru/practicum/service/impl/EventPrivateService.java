package ru.practicum.service.impl;

import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.event.UpdateEventUserRequest;
import ru.practicum.dto.event.UpdateEventUserRequestOutput;

import java.util.List;

public interface EventPrivateService {
    EventFullDto addEventDto(NewEventDto newEventDto, Long userId);

    List<EventFullDto> getEventByUserId(Long userId, int from, int size);

    EventFullDto getEventForVerificationUser(Long userId, Long eventId);

    UpdateEventUserRequestOutput updateEvent(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest);
}