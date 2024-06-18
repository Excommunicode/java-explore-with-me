package ru.practicum.service.impl;

import ru.practicum.dto.registration.EventRequestStatusUpdateRequest;
import ru.practicum.dto.registration.EventRequestStatusUpdateResult;
import ru.practicum.dto.registration.ParticipationRequestDto;

import java.util.List;

public interface ParticipationPrivateService {

    ParticipationRequestDto addRequest(Long userId, Long eventId);

    List<ParticipationRequestDto> findInformationAboutUserRegistration(Long userId);

    List<ParticipationRequestDto> findRequestRegistration(Long userId, Long eventId);

    EventRequestStatusUpdateResult changeStateRequests(Long userId, Long eventId, EventRequestStatusUpdateRequest newRequestsEvent);

    ParticipationRequestDto cancelingYourRequest(Long userId, Long requestId);
}