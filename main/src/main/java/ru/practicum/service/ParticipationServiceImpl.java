package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.registration.EventRequestStatusUpdateRequest;
import ru.practicum.dto.registration.EventRequestStatusUpdateResult;
import ru.practicum.dto.registration.ParticipationRequestDto;
import ru.practicum.enums.ParticipationRequestStatus;
import ru.practicum.enums.State;
import ru.practicum.exceptiion.ConflictException;
import ru.practicum.exceptiion.NotFoundException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.mapper.ParticipationMapper;
import ru.practicum.model.ParticipationRequest;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.ParticipationRepository;
import ru.practicum.service.api.ParticipationPrivateService;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static ru.practicum.enums.ParticipationRequestStatus.CANCELED;
import static ru.practicum.enums.ParticipationRequestStatus.CONFIRMED;
import static ru.practicum.enums.ParticipationRequestStatus.PENDING;
import static ru.practicum.enums.ParticipationRequestStatus.REJECTED;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
public class ParticipationServiceImpl implements ParticipationPrivateService {
    private final ParticipationRepository participationRepository;
    private final ParticipationMapper participationMapper;
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final EntityManager entityManager;


    @Transactional
    @Override
    public ParticipationRequestDto addRequest(Long userId, Long eventId) {
        log.debug("Adding participation request for user {} and event {}", userId, eventId);

        EventFullDto eventFullDto = findEventById(eventId);

        checkRepeatedRequest(userId, eventId);
        checkUserIsInitiatorEvent(userId, eventFullDto);
        checkEventIsPublished(eventFullDto);
        checkParticipationLimit(eventFullDto);

        ParticipationRequestStatus state = getParticipationRequestStatus(eventFullDto);
        LocalDateTime localDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        ParticipationRequestDto result = participationMapper.toDto(
                participationRepository.save(participationMapper.toModel(userId, eventId, localDateTime, state)));

        log.info("Participation request added: {}", result);
        return result;
    }

    @Transactional
    @Override
    public EventRequestStatusUpdateResult changeStateRequests(Long userId, Long eventId, EventRequestStatusUpdateRequest newRequestsEvent) {
        log.debug("Changing state of participation requests for user {} on event {}", userId, eventId);

        List<ParticipationRequest> eventRegistrations = participationRepository.findAllByIdIn(newRequestsEvent.getRequestIds());
        EventFullDto eventFullDto = findEventById(eventId);

        checkUserIsInitiator(userId, eventId, eventFullDto);
        validParticipation(newRequestsEvent, eventFullDto, eventRegistrations);

        participationRepository.updateStatusByParticipationIds(newRequestsEvent.getRequestIds(), newRequestsEvent.getStatus());
        eventRepository.updateConfirmedRequestsById(eventId, newRequestsEvent.getRequestIds().size());

        entityManager.clear();

        List<ParticipationRequestDto> events = participationMapper.toListDto(participationRepository.findAll());

        List<ParticipationRequestDto> confirmedRequests = events.parallelStream()
                .filter(event -> event.getStatus() == CONFIRMED)
                .collect(Collectors.toList());

        List<ParticipationRequestDto> rejectedRequests = events.parallelStream()
                .filter(event -> event.getStatus() == REJECTED)
                .collect(Collectors.toList());

        confirmedRequests.sort(Comparator.comparing(ParticipationRequestDto::getId).reversed());
        rejectedRequests.sort(Comparator.comparing(ParticipationRequestDto::getId).reversed());

        EventRequestStatusUpdateResult result = EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmedRequests)
                .rejectedRequests(rejectedRequests)
                .build();

        log.info("Request status updated. Confirmed: {}, Rejected: {}", confirmedRequests.size(), rejectedRequests.size());
        return result;
    }

    @Transactional
    @Override
    public ParticipationRequestDto cancelingYourRequest(Long userId, Long requestId) {
        log.debug("Canceling request {} for user {}", requestId, userId);

        ParticipationRequestDto request = participationMapper.toDto(participationRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(String.format("Request with id %s not found", requestId))));

        request.setStatus(CANCELED);
        participationRepository.updateByIdStatus(request.getId(), request.getStatus());

        log.info("Request {} canceled", requestId);
        return request;
    }

    @Override
    public List<ParticipationRequestDto> findInformationAboutUserRegistration(Long userId) {
        log.debug("Retrieving registration information for user {}", userId);
        List<ParticipationRequestDto> result = participationMapper.toListDto(participationRepository.findAllByRequester_Id(userId));
        log.debug("Found {} registrations for user {}", result.size(), userId);
        return result;
    }

    @Override
    public List<ParticipationRequestDto> findRequestRegistration(Long userId, Long eventId) {
        log.debug("Searching for request registrations for user {} and event {}", userId, eventId);
        List<ParticipationRequestDto> listDto = participationMapper.toListDto(participationRepository.findAllByEventInitiatorIdAndEventId(userId, eventId));
        if (listDto.isEmpty()) {
            log.warn("No registrations found for user {} and event {}", userId, eventId);
            return Collections.emptyList();
        }
        log.debug("Registrations found for user {} and event {}: {}", userId, eventId, listDto.size());
        return listDto;
    }


    private EventFullDto findEventById(Long eventId) {
        return eventMapper.toFullDto(eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id %s not found", eventId))));
    }


    private void checkParticipationLimit(EventFullDto eventFullDto) {
        if (eventFullDto.getConfirmedRequests() >= eventFullDto.getParticipantLimit() && eventFullDto.getParticipantLimit() != 0) {
            throw new ConflictException("Application limit has been exhausted");
        }
    }

    private ParticipationRequestStatus getParticipationRequestStatus(EventFullDto eventFullDto) {
        ParticipationRequestStatus state;
        if (!eventFullDto.getRequestModeration() || eventFullDto.getParticipantLimit() == 0) {
            eventFullDto.setConfirmedRequests(eventFullDto.getParticipantLimit() + 1);
            state = CONFIRMED;
            eventRepository.save(eventMapper.toModelFromFullDto(eventFullDto));
        } else {
            state = PENDING;
        }
        return state;
    }

    private void checkRepeatedRequest(Long userId, Long eventId) {
        if (participationRepository.existsByRequester_IdAndEvent_id(userId, eventId)) {
            throw new ConflictException("You cannot send a repeat request");
        }
    }

    private static void checkUserIsInitiator(Long userId, Long eventId, EventFullDto eventFullDto) {
        if (!Objects.equals(eventFullDto.getInitiator().getId(), userId)) {
            throw new ConflictException(String.format("User with id %s is not initiator for event with id %s", userId, eventId));
        }
    }

    private void checkUserIsInitiatorEvent(Long userId, EventFullDto eventFullDto) {
        if (Objects.equals(userId, eventFullDto.getInitiator().getId())) {
            throw new ConflictException("Initiator of the event cannot participate in his own event");
        }
    }

    private void checkEventIsPublished(EventFullDto eventFullDto) {
        if (!Objects.equals(eventFullDto.getState().toString(), State.PUBLISHED.toString())) {
            throw new ConflictException(String.format("Event with id :%s is not published", eventFullDto.getId()));
        }
    }

    private static void validParticipation(EventRequestStatusUpdateRequest newRequestsEvent, EventFullDto eventFullDto, List<ParticipationRequest> eventRegistrations) {
        switch (newRequestsEvent.getStatus()) {
            case CONFIRMED:
                eventFullDto.setConfirmedRequests(eventFullDto.getConfirmedRequests() + newRequestsEvent.getRequestIds().size());
            case CANCELED:
                eventFullDto.setConfirmedRequests(eventFullDto.getConfirmedRequests() - newRequestsEvent.getRequestIds().size());
        }
        if (eventFullDto.getConfirmedRequests() >= eventFullDto.getParticipantLimit() && eventFullDto.getParticipantLimit() != 0) {
            throw new ConflictException("Limit of requests for applications has been exceeded");
        }
        for (ParticipationRequest eventRegistration : eventRegistrations) {
            if (eventRegistration.getStatus() == CONFIRMED) {
                throw new ConflictException(String.format("Request with id %s has already been confirmed", eventRegistration.getId()));
            }
        }
    }
}