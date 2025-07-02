package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.EndpointDto;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.event.UpdateEventUserRequest;
import ru.practicum.dto.event.UpdateEventUserRequestOutput;
import ru.practicum.exceptiion.BadRequestException;
import ru.practicum.exceptiion.ConflictException;
import ru.practicum.exceptiion.NotFoundException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.Location;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.LocationRepository;
import ru.practicum.repository.UserRepository;
import ru.practicum.service.api.EventPrivateService;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static ru.practicum.constant.EventConstant.DATE_TIME_FORMATTER;
import static ru.practicum.constant.EventConstant.EVM_SERVICE;
import static ru.practicum.enums.State.PENDING;
import static ru.practicum.enums.State.PUBLISHED;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventPrivateServiceImpl implements EventPrivateService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final StatsClient statsClient;


    @Transactional
    @Override
    public EventFullDto addEventDto(NewEventDto newEventDto, Long userId) {
        log.debug("Starting to add a new event by user ID: {}", userId);

        checkEventDate(parseLocalDateTime(newEventDto.getEventDate()));
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
        EventFullDto eventFullDto = findEvenFullDtoById(eventId);

        validateUpdateRequest(userId, eventFullDto, updateEventUserRequest);
        EventFullDto updateEvent = eventMapper.updateEventByUser(eventFullDto, updateEventUserRequest);

        UpdateEventUserRequestOutput fullDto = eventMapper.toUpdateDtoOutput(eventRepository.save(eventMapper.toModelFromFullDto(updateEvent)));

        log.info("Update completed for Event with ID: {}", fullDto.getId());
        return fullDto;
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

        if (!eventRepository.existsByIdAndInitiator_Id(eventId, userId)) {
            recordRequestStats(httpServletRequest);
        }

        return findEvenFullDtoById(eventId);
    }

    private void checkEventDate(LocalDateTime dateTime) {
        if (dateTime != null) {
            if (dateTime.isBefore(LocalDateTime.now())) {
                throw new BadRequestException("you cannot change the date of the event to a past date");
            }
        }
    }

    private LocalDateTime parseLocalDateTime(String dataTime) {
        return dataTime != null ? LocalDateTime.parse(dataTime, DateTimeFormatter.ofPattern(DATE_TIME_FORMATTER)) : null;
    }

    private void findUserById(Long userId) {

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(String.format("User with id %s was not found", userId));
        }

    }

    private EventFullDto findEvenFullDtoById(Long eventId) {

        return eventMapper.toFullDto(eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id %s not found", eventId))));

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

    private void validateUpdateRequest(Long userId, EventFullDto event, UpdateEventUserRequest updateEventUserRequest) {

        if (updateEventUserRequest.getParticipantLimit() != null && updateEventUserRequest.getParticipantLimit() < 0) {
            throw new BadRequestException("ParticipantLimit cannot be negative or null");
        }

        if (!event.getInitiator().getId().equals(userId)) {
            throw new BadRequestException("Unauthorized attempt to update event by user ID: " + userId);
        }

        if (event.getState() == PUBLISHED) {
            throw new ConflictException("You cannot change a published event");
        }
        checkEventDate(parseLocalDateTime(updateEventUserRequest.getEventDate()));
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
}
