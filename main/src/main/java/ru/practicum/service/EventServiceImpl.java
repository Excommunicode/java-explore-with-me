package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.EndpointDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.dto.event.*;
import ru.practicum.enums.EventSort;
import ru.practicum.enums.ParticipationRequestStatus;
import ru.practicum.enums.StateAction;
import ru.practicum.exceptiion.BadRequestException;
import ru.practicum.exceptiion.ConflictException;
import ru.practicum.exceptiion.NotFoundException;
import ru.practicum.mapper.CategoryMapper;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.Event;
import ru.practicum.model.Location;
import ru.practicum.model.ParticipationRequest;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.LocationRepository;
import ru.practicum.repository.ParticipationRepository;
import ru.practicum.repository.UserRepository;
import ru.practicum.service.impl.EventAdminService;
import ru.practicum.service.impl.EventPrivateService;
import ru.practicum.service.impl.EventPublicService;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static ru.practicum.constant.EventConstant.DATE_TIME_FORMATTER;
import static ru.practicum.constant.EventConstant.EVM_SERVICE;
import static ru.practicum.enums.State.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
public class EventServiceImpl implements EventPrivateService, EventPublicService, EventAdminService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final CategoryMapper categoryMapper;
    private final ParticipationRepository participationRepository;
    private final StatsClient statsClient;


    @Transactional
    @Override
    public EventFullDto addEventDto(NewEventDto newEventDto, Long userId) {
        log.debug("Starting to add a new event by user ID: {}", userId);

        checkEventDate(parseLocalDateTime(newEventDto));
        findUserById(userId);
        verifyLocation(newEventDto);

        EventFullDto fullDto = eventMapper.toFullDto(eventRepository.save(eventMapper.toEvent(newEventDto, userId, PENDING, LocalDateTime.now())));

        log.info("Event saved with ID: {}", fullDto.getId());
        return fullDto;
    }

    @Transactional
    @Override
    public UpdateEventUserRequestOutput updateEvent(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) {
        log.debug("Updating event with ID: {}", eventId);
        Event event = findEventById(eventId);

        validateUpdateRequest(userId, event, updateEventUserRequest);
        applyEventUpdates(event, updateEventUserRequest);

        UpdateEventUserRequestOutput fullDto = eventMapper.toUpdateDtoOutput(eventRepository.save(event));
        log.info("Update completed for Event with ID: {}", fullDto.getId());
        return fullDto;
    }

    @Transactional
    @Override
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateEventAdminRequest) {
        Event event = findEventById(eventId);

        applyAdminEventUpdates(event, updateEventAdminRequest);

        EventFullDto fullDto = eventMapper.toFullDto(eventRepository.save(event));
        log.info("Update by admin completed for Event with ID: {}", fullDto.getId());
        return fullDto;
    }


    @Override
    public List<EventFullDto> findEventForAdmin(List<Long> users, List<String> states, List<Long> categories,
                                                LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size) {

        Pageable pageable = PageRequest.of(from, size, Sort.by(Sort.Direction.DESC, "id"));

        List<EventFullDto> eventFullDtos = new ArrayList<>();


        if (Objects.nonNull(rangeStart) || Objects.nonNull(states)) {
            eventFullDtos = eventMapper.toDtoList(eventRepository.findAllByStateAndEventDate(states, rangeStart, rangeEnd, from, size));
        }

        if (Objects.nonNull(users) || Objects.nonNull(categories)) {
            eventFullDtos = eventMapper.toDtoList(
                    eventRepository.findAllByInitiator_IdInAndCategory(users, categories, from, size));
        }

        List<Long> collect = eventFullDtos.stream()
                .map(EventFullDto::getId)
                .collect(Collectors.toList());

        List<ParticipationRequest> allByEventIdIn = participationRepository.findAllByEvent_IdInAndAndStatus(collect, ParticipationRequestStatus.CONFIRMED);

        if (eventFullDtos.isEmpty()) {
            eventFullDtos = eventMapper.toDtoList(eventRepository.findAll(pageable).getContent());
        }

        int x = 0;
        for (EventFullDto eventFullDto : eventFullDtos) {
            for (ParticipationRequest participationRequest : allByEventIdIn) {
                if (Objects.equals(eventFullDto.getId(), participationRequest.getEvent().getId())) {
                    x++;
                }
            }
            eventFullDto.setConfirmedRequests(x);
        }

        return eventFullDtos;
    }

    @Override
    public List<EventFullDto> getEventByUserId(Long userId, int from, int size) {
        log.debug("Fetching events initiated by user ID: {}, page: {}, size: {}", userId, from, size);
        Pageable pageable = PageRequest.of(from, size);
        List<EventFullDto> dtoList = eventMapper.toDtoList(eventRepository.findAllByInitiator_Id(userId, pageable));
        if (dtoList.isEmpty()) {
            log.info("No events found for user ID: {}", userId);
            return Collections.emptyList();
        }
        log.debug("Found {} events for user ID: {}", dtoList.size(), userId);
        return dtoList;
    }

    @Override
    public EventFullDto getEventForVerificationUser(Long userId, Long eventId) {
        log.debug("Verifying event with ID: {} for user ID: {}", eventId, userId);
        if (!userRepository.existsById(userId)) {
            log.warn("User verification failed for user ID: {}", userId);
            throw new BadRequestException("Invalid user ID: " + userId);
        }
        return eventMapper.toFullDto(eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found")));
    }

    @Override
    public List<EventFullDto> getEventsDto(String text, List<Long> categories, boolean paid,
                                           LocalDateTime rangeStart, LocalDateTime rangeEnd, boolean onlyAvailable,
                                           EventSort sort, int from, int size, HttpServletRequest httpServletRequest) {

        Specification<Event> specification = Specification.where(null);


        Pageable pageable = PageRequest.of(from, size, Sort.by(Sort.Direction.DESC, "id"));

        specification = applyTextFilter(specification, text);
        specification = applyCategoryFilter(specification, categories);
        specification = applyDateRangeFilter(specification, rangeStart, rangeEnd);
        specification = applyAvailabilityFilter(specification, onlyAvailable);

        List<EventFullDto> dtoList = eventMapper.toDtoList(
                eventRepository.findAll(specification, pageable).getContent());

        statsClient.postStats(EndpointDto.builder()
                .app(EVM_SERVICE)
                .uri(httpServletRequest.getRequestURI())
                .ip(httpServletRequest.getRemoteAddr())
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMATTER)))
                .build());
        return dtoList;
    }

    @Override
    public EventFullDto getEventByIdPublic(Long id, HttpServletRequest httpServletRequest) {
        log.debug("Request to receive an event with id: {}", id);

        EventFullDto event = eventMapper.toFullDto(eventRepository.findByIdAndState(id, PUBLISHED)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id %s not found", id))));

        statsClient.postStats(EndpointDto.builder()
                .app(EVM_SERVICE)
                .uri(httpServletRequest.getRequestURI())
                .ip(httpServletRequest.getRemoteAddr())
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMATTER)))
                .build());

        String uri = "/events/" + event.getId();
        List<String> uris = List.of(uri);


        String startTime = event.getPublishedOn().minusHours(1).format(DateTimeFormatter.ofPattern(DATE_TIME_FORMATTER));
        String endTime = LocalDateTime.now().plusMinutes(1).format(DateTimeFormatter.ofPattern(DATE_TIME_FORMATTER));

        List<ViewStatsDto> stats = statsClient.getStats(startTime, endTime, uris, true);

        if (!stats.isEmpty()) {
            event.setViews(stats.get(0).getHits());
        }
        return event;
    }

    private LocalDateTime parseLocalDateTime(NewEventDto newEventDto) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMATTER);
        return LocalDateTime.parse(newEventDto.getEventDate(), formatter);
    }

    private void verifyLocation(NewEventDto newEventDto) {
        Location byLatAndLon = locationRepository.findByLatAndLon(
                newEventDto.getLocation().getLat(), newEventDto.getLocation().getLon());
        if (byLatAndLon == null) {
            Location managedLocation = locationRepository.save(newEventDto.getLocation());
            newEventDto.setLocation(managedLocation);
            log.debug("Saved new location for event");
        } else {
            newEventDto.setLocation(byLatAndLon);
            log.debug("Using existing location for event");
        }
    }

    private void checkEventAlreadyPublished(Long eventId) {
        EventFullDto eventFullDto = findEvenFullDtoById(eventId);
        if (Objects.requireNonNull(eventFullDto.getState()) == PUBLISHED ||
                Objects.requireNonNull(eventFullDto.getState()) == CANCELED) {
            throw new ConflictException("Event has already published");
        }
    }

    private void checkEventIsPublished(Long eventId) {
        EventFullDto eventFullDto = findEvenFullDtoById(eventId);
        if (Objects.requireNonNull(eventFullDto.getState()) == PUBLISHED) {
            throw new ConflictException("Event already been published at the moment");
        }
    }

    private EventFullDto findEvenFullDtoById(Long eventId) {
        return eventMapper.toFullDto(eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found")));
    }

    private void findUserById(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User was not found");
        }
    }

    private Specification<Event> applyTextFilter(Specification<Event> specification, String text) {

        if (text != null) {
            String searchText = "%" + text.toLowerCase() + "%";
            return specification.and((root, query, criteriaBuilder) -> criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), searchText),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("annotation")), searchText)
            ));
        }
        return specification;
    }

    private Specification<Event> applyCategoryFilter(Specification<Event> specification, List<Long> categories) {

        if (categories != null && !categories.isEmpty()) {
            return specification.and((root, query, criteriaBuilder) ->
                    root.get("category").get("id").in(categories)
            );
        }
        return specification;
    }

    private Specification<Event> applyDateRangeFilter(Specification<Event> specification,
                                                      LocalDateTime rangeStart, LocalDateTime rangeEnd) {

        if (rangeStart != null && rangeEnd != null) {

            checkStartTime(rangeStart, rangeEnd);

            return specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.between(root.get("eventDate"), rangeStart, rangeEnd)
            );
        }
        return specification;
    }

    private Specification<Event> applyAvailabilityFilter(Specification<Event> specification, boolean onlyAvailable) {

        if (onlyAvailable) {
            return specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThan(root.get("participantLimit"), 0)
            );
        }
        return specification;
    }

    private void checkStartTime(LocalDateTime rangeStart, LocalDateTime rangeEnd) {

        if (rangeStart.isAfter(rangeEnd)) {
            throw new BadRequestException("Start cannot be later than the end");
        }
    }

    private void checkEventDate(LocalDateTime dateTime) {
        if (dateTime.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("you cannot change the date of the event to a past date");
        }
    }

    private Event findEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id %s not found", eventId)));
    }

    private void validateUpdateRequest(Long userId, Event event, UpdateEventUserRequest updateEventUserRequest) {
        if (updateEventUserRequest.getParticipantLimit() != null && updateEventUserRequest.getParticipantLimit() < 0) {
            throw new BadRequestException("ParticipantLimit cannot be negative");
        }
        if (!event.getInitiator().getId().equals(userId)) {
            throw new BadRequestException("Unauthorized attempt to update event by user ID: " + userId);
        }
        if (event.getState() == PUBLISHED) {
            throw new ConflictException("You cannot change a published event");
        }
    }

    private void applyEventUpdates(Event event, UpdateEventUserRequest updateEventUserRequest) {
        if (updateEventUserRequest.getAnnotation() != null) {
            event.setAnnotation(updateEventUserRequest.getAnnotation());
        }
        if (updateEventUserRequest.getCategoryDto() != null) {
            event.setCategory(categoryMapper.toModel(updateEventUserRequest.getCategoryDto()));
        }
        if (updateEventUserRequest.getDescription() != null) {
            event.setDescription(updateEventUserRequest.getDescription());
        }
        if (updateEventUserRequest.getEventDate() != null) {
            LocalDateTime dateTime = parseEventDate(updateEventUserRequest.getEventDate());
            checkEventDate(dateTime);
            event.setEventDate(dateTime);
        }
        if (updateEventUserRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateEventUserRequest.getRequestModeration());
        }
        if (updateEventUserRequest.getStateAction() != null) {
            applyStateAction(event, updateEventUserRequest.getStateAction());
        }
        if (updateEventUserRequest.getTitle() != null) {
            event.setTitle(updateEventUserRequest.getTitle());
        }
    }

    private LocalDateTime parseEventDate(String dateTimeString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMATTER);
        return LocalDateTime.parse(dateTimeString, formatter);
    }

    private void applyStateAction(Event event, StateAction stateAction) {
        switch (stateAction) {
            case PUBLISH_EVENT:
                event.setState(PUBLISHED);
                break;
            case SEND_TO_REVIEW:
                event.setState(PENDING);
                break;
            case CANCEL_REVIEW:
            case REJECT_EVENT:
                event.setState(CANCELED);
                break;
        }
    }

    private void applyAdminEventUpdates(Event event, UpdateEventAdminRequest updateEventAdminRequest) {
        if (updateEventAdminRequest.getAnnotation() != null) {
            event.setAnnotation(updateEventAdminRequest.getAnnotation());
        }
        if (updateEventAdminRequest.getCategoryDto() != null) {
            event.setCategory(categoryMapper.toModel(updateEventAdminRequest.getCategoryDto()));
        }
        if (updateEventAdminRequest.getDescription() != null) {
            event.setDescription(updateEventAdminRequest.getDescription());
        }
        if (updateEventAdminRequest.getEventDate() != null) {
            LocalDateTime dateTime = parseEventDate(updateEventAdminRequest.getEventDate());
            checkEventDate(dateTime);
            event.setEventDate(dateTime);
        }
        if (updateEventAdminRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventAdminRequest.getParticipantLimit());
        }
        if (updateEventAdminRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateEventAdminRequest.getRequestModeration());
        }
        if (updateEventAdminRequest.getPaid() != null) {
            event.setPaid(updateEventAdminRequest.getPaid());
        }
        if (updateEventAdminRequest.getStateAction() != null) {
            applyAdminStateAction(event, updateEventAdminRequest.getStateAction(), event.getId());
        }
        if (updateEventAdminRequest.getTitle() != null) {
            event.setTitle(updateEventAdminRequest.getTitle());
        }
    }

    private void applyAdminStateAction(Event event, StateAction stateAction, Long eventId) {
        switch (stateAction) {
            case PUBLISH_EVENT:
                checkEventAlreadyPublished(eventId);
                event.setState(PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
                break;
            case SEND_TO_REVIEW:
                event.setState(PENDING);
                break;
            case CANCEL_REVIEW:
            case REJECT_EVENT:
                checkEventIsPublished(eventId);
                event.setState(CANCELED);
                break;
        }
    }
}