package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.EndpointDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.event.UpdateEventAdminRequest;
import ru.practicum.dto.event.UpdateEventUserRequest;
import ru.practicum.dto.event.UpdateEventUserRequestOutput;
import ru.practicum.dto.rating.RatingDto;
import ru.practicum.enums.EventSort;
import ru.practicum.enums.ParticipationRequestStatus;
import ru.practicum.enums.StateAction;
import ru.practicum.exceptiion.BadRequestException;
import ru.practicum.exceptiion.ConflictException;
import ru.practicum.exceptiion.NotFoundException;
import ru.practicum.mapper.CategoryMapper;
import ru.practicum.mapper.EventMapper;
import ru.practicum.mapper.RatingMapper;
import ru.practicum.model.Event;
import ru.practicum.model.Location;
import ru.practicum.model.ParticipationRequest;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.LocationRepository;
import ru.practicum.repository.ParticipationRepository;
import ru.practicum.repository.RatingRepository;
import ru.practicum.repository.UserRepository;
import ru.practicum.service.impl.EventAdminService;
import ru.practicum.service.impl.EventPrivateService;
import ru.practicum.service.impl.EventPublicService;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static ru.practicum.constant.EventConstant.DATE_TIME_FORMATTER;
import static ru.practicum.constant.EventConstant.EVM_SERVICE;
import static ru.practicum.enums.State.CANCELED;
import static ru.practicum.enums.State.PENDING;
import static ru.practicum.enums.State.PUBLISHED;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(
        readOnly = true,
        isolation = Isolation.REPEATABLE_READ,
        propagation = Propagation.REQUIRED
)
public class EventServiceImpl implements EventPrivateService, EventPublicService, EventAdminService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final CategoryMapper categoryMapper;
    private final ParticipationRepository participationRepository;
    private final RatingRepository ratingRepository;
    private final RatingMapper ratingMapper;
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
        log.debug("Updating event by admin with ID: {}", eventId);
        Event event = findEventById(eventId);

        applyAdminEventUpdates(event, updateEventAdminRequest);

        EventFullDto fullDto = eventMapper.toFullDto(eventRepository.save(event));
        log.info("Update by admin completed for Event with ID: {}", fullDto.getId());
        return fullDto;
    }


    @Override
    public List<EventFullDto> findEventForAdmin(List<Long> users, List<String> states, List<Long> categories,
                                                LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size) {

        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size, Sort.by(Sort.Direction.DESC, "id"));

        List<EventFullDto> eventFullDtoArrayList;

        if (Objects.nonNull(rangeStart) && Objects.nonNull(states)) {
            eventFullDtoArrayList = eventMapper.toDtoList(
                    eventRepository.findAllByStateAndEventDate(states, rangeStart, rangeEnd, pageable));
        } else if (Objects.nonNull(users) && Objects.nonNull(categories)) {
            eventFullDtoArrayList = eventMapper.toDtoList(
                    eventRepository.findAllByInitiator_IdInAndCategory(users, categories, pageable));
        } else {
            eventFullDtoArrayList = eventMapper.toDtoList(eventRepository.findAll(pageable).getContent());
        }

        List<Long> eventsIds = getEventIds(eventFullDtoArrayList);

        List<ParticipationRequest> participationRequests = participationRepository.findAllByEventIdInAndStatus(eventsIds, ParticipationRequestStatus.CONFIRMED);

        for (EventFullDto eventFullDto : eventFullDtoArrayList) {
            long confirmedRequestsCount = participationRequests.stream()
                    .filter(x -> Objects.equals(eventFullDto.getId(), x.getEvent().getId()))
                    .count();
            eventFullDto.setConfirmedRequests((int) confirmedRequestsCount);
        }

        return eventFullDtoArrayList;
    }

    @Override
    public List<EventFullDto> getEventByUserId(Long userId, int from, int size) {
        log.debug("Fetching events initiated by user ID: {}, page: {}, size: {}", userId, from, size);

        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size);
        List<EventFullDto> dtoList = eventMapper.toDtoList(eventRepository.findAllByInitiator_Id(userId, pageable));

        if (dtoList.isEmpty()) {
            log.info("No events found for user ID: {}", userId);
            return Collections.emptyList();
        }

        log.debug("Found {} events for user ID: {}", dtoList.size(), userId);
        return dtoList;
    }

    @Override
    public EventFullDto getEventForVerificationUser(Long userId, Long eventId, HttpServletRequest httpServletRequest) {
        log.debug("Verifying event with ID: {} for user ID: {}", eventId, userId);

        if (!eventRepository.existsByIdAndInitiator(userId, eventId)) {
            recordRequestStats(httpServletRequest);
        }

        return findEvenFullDtoById(eventId);
    }

    @Transactional
    @Override
    public List<EventFullDto> getEventsDto(String text, List<Long> categories, boolean paid,
                                           LocalDateTime rangeStart, LocalDateTime rangeEnd, boolean onlyAvailable,
                                           EventSort sort, int from, int size, HttpServletRequest httpServletRequest) {

        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size, Sort.by(Sort.Direction.DESC, "id"));
        List<EventFullDto> dtoList = findEventByParameters(text, categories, paid, rangeStart, rangeEnd, pageable);

        List<EventFullDto> eventFullDtoList = setAverageRatings(sort, dtoList, getEventIds(dtoList));
        recordRequestStats(httpServletRequest);

        return eventFullDtoList.isEmpty() ? dtoList : eventFullDtoList;
    }


    @Transactional
    @Override
    public EventFullDto getEventByIdPublic(Long id, HttpServletRequest httpServletRequest) {
        log.debug("Request to receive an event with id: {}", id);

        EventFullDto event = eventMapper.toFullDto(eventRepository.findByIdAndState(id, PUBLISHED)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id %s not found", id))));

        recordRequestStats(httpServletRequest);

        String uri = "/events/" + event.getId();
        List<String> uris = List.of(uri);

        String startTime = event.getPublishedOn().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMATTER));
        String endTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMATTER));

        List<ViewStatsDto> stats = statsClient.getStats(startTime, endTime, uris, true);

        if (!stats.isEmpty()) {
            event.setViews(stats.get(0).getHits());
        }
        return event;
    }

    private List<EventFullDto> setAverageRatings(EventSort sort, List<EventFullDto> dtoList, List<Long> eventsIds) {
        if (Objects.isNull(sort)) {
            return Collections.emptyList();
        }
        if (sort == EventSort.RATINGS) {
            List<RatingDto> ratingMapperDtoList = ratingMapper.toDtoList(
                    ratingRepository.findAllByEventIdInAndAssessmentIsNotNull(eventsIds));
            for (EventFullDto eventFullDto : dtoList) {

                List<RatingDto> ratingsForEvent = ratingMapperDtoList.stream()
                        .filter(ratingDto -> Objects.equals(eventFullDto.getId(), ratingDto.getEventId()))
                        .collect(Collectors.toList());

                double averageRating = ratingsForEvent.stream()
                        .mapToDouble(RatingDto::getAssessment)
                        .average()
                        .orElse(0.0);

                eventFullDto.setAssessment(averageRating);
            }
        }
        dtoList.sort(Comparator.comparingDouble(EventFullDto::getAssessment).reversed());
        return dtoList;
    }

    private static List<Long> getEventIds(List<EventFullDto> eventFullDtoArrayList) {

        return eventFullDtoArrayList.stream()
                .map(EventFullDto::getId)
                .collect(Collectors.toList());
    }

    private LocalDateTime parseLocalDateTime(NewEventDto newEventDto) {

        return LocalDateTime.parse(newEventDto.getEventDate(), DateTimeFormatter.ofPattern(DATE_TIME_FORMATTER));
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
            throw new ConflictException(String.format("Event with id %s already been published at the moment", eventId));
        }
    }

    private EventFullDto findEvenFullDtoById(Long eventId) {
        return eventMapper.toFullDto(eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id %s not found", eventId))));
    }

    private void findUserById(Long userId) {

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(String.format("User with id %s was not found", userId));
        }
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
            throw new BadRequestException("ParticipantLimit cannot be negative or null");
        }

        if (!event.getInitiator().getId().equals(userId)) {
            throw new BadRequestException("Unauthorized attempt to update event by user ID: " + userId);
        }

        if (event.getState() == PUBLISHED) {
            throw new ConflictException("You cannot change a published event");
        }

    }

    private void applyEventUpdates(Event event, UpdateEventUserRequest updateEventUserRequest) {

        if (Objects.nonNull(updateEventUserRequest.getAnnotation())) {
            event.setAnnotation(updateEventUserRequest.getAnnotation());
        }

        if (Objects.nonNull(updateEventUserRequest.getCategoryDto())) {
            event.setCategory(categoryMapper.toModel(updateEventUserRequest.getCategoryDto()));
        }

        if (Objects.nonNull(updateEventUserRequest.getDescription())) {
            event.setDescription(updateEventUserRequest.getDescription());
        }

        if (Objects.nonNull(updateEventUserRequest.getEventDate())) {
            LocalDateTime dateTime = parseEventDate(updateEventUserRequest.getEventDate());
            checkEventDate(dateTime);
            event.setEventDate(dateTime);
        }

        if (Objects.nonNull(updateEventUserRequest.getRequestModeration())) {
            event.setRequestModeration(updateEventUserRequest.getRequestModeration());
        }

        if (Objects.nonNull(updateEventUserRequest.getStateAction())) {
            applyStateAction(event, updateEventUserRequest.getStateAction());
        }

        if (Objects.nonNull(updateEventUserRequest.getTitle())) {
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

    private void recordRequestStats(HttpServletRequest httpServletRequest) {
        if (Objects.nonNull(httpServletRequest)) {
            statsClient.postStats(EndpointDto.builder()
                    .app(EVM_SERVICE)
                    .uri(httpServletRequest.getRequestURI())
                    .ip(httpServletRequest.getRemoteAddr())
                    .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMATTER)))
                    .build());
        }
    }

    private List<EventFullDto> findEventByParameters(String text, List<Long> categories, boolean paid, LocalDateTime rangeStart, LocalDateTime rangeEnd, Pageable pageable) {
        List<EventFullDto> dtoList = new ArrayList<>();
        if (Objects.nonNull(text)) {
            String textLowerCase = text.toLowerCase();
            if (Objects.nonNull(rangeStart) || Objects.nonNull(rangeEnd)) {
                checkStartTime(rangeStart, rangeEnd);
                dtoList = eventMapper.toDtoList(eventRepository.findAllByDescriptionAndCategoryAndPaidAndPaid(textLowerCase, categories, paid, LocalDateTime.now(), pageable));
            } else {
                dtoList = eventMapper.toDtoList(eventRepository.findAllByEventDateAndDescriptionAndPaid(textLowerCase, categories, rangeStart, rangeEnd, paid, pageable));
            }
        } else if (categories != null) {
            if (!categories.isEmpty()) {
                dtoList = eventMapper.toDtoList(eventRepository.findAllByCategoryIdInAndPaid(categories, pageable));
            }
        }
        return dtoList;
    }
}