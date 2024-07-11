package ru.practicum.service;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
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
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.UpdateEventAdminRequest;
import ru.practicum.dto.registration.ParticipationRequestDto;
import ru.practicum.enums.ParticipationRequestStatus;
import ru.practicum.enums.State;
import ru.practicum.exceptiion.BadRequestException;
import ru.practicum.exceptiion.ConflictException;
import ru.practicum.exceptiion.NotFoundException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.mapper.ParticipationMapper;
import ru.practicum.model.QEvent;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.ParticipationRepository;
import ru.practicum.service.api.EventAdminService;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static ru.practicum.constant.EventConstant.DATE_TIME_FORMATTER;
import static ru.practicum.constant.EventConstant.EVM_SERVICE;
import static ru.practicum.enums.State.CANCELED;
import static ru.practicum.enums.State.PUBLISHED;
import static ru.practicum.enums.StateAction.PUBLISH_EVENT;
import static ru.practicum.enums.StateAction.REJECT_EVENT;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
public class EventAdminServiceImpl implements  EventAdminService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final ParticipationRepository participationRepository;
    private final ParticipationMapper participationMapper;
    private final StatsClient statsClient;


    @Transactional
    @Override
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateEventAdminRequest) {
        log.debug("Updating event by admin with ID: {}", eventId);
        checkEventDateUpdate(updateEventAdminRequest.getEventDate());


        EventFullDto event = findEvenFullDtoById(eventId);
        checkEventAlreadyPublished(event, updateEventAdminRequest);
        checkEventIsPublished(event, updateEventAdminRequest);
        EventFullDto eventFullDto = eventMapper.updateEventByAdmin(event, updateEventAdminRequest);


        EventFullDto fullDto = eventMapper.toFullDto(eventRepository.save(eventMapper.toModelFromFullDto(eventFullDto)));

        log.info("Update by admin completed for Event with ID: {}", fullDto.getId());
        return fullDto;
    }

    @Override
    public List<EventFullDto> findEventForAdmin(List<Long> users, List<String> states, List<Long> categories,
                                                LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size) {

        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size, Sort.by(Sort.Direction.DESC, "id"));

        BooleanBuilder builder = new BooleanBuilder();
        QEvent event = QEvent.event;

        if (Objects.nonNull(users) && !users.isEmpty()) {
            BooleanExpression byUsers = event.initiator.id.in(users);
            builder.and(byUsers);
        }

        if (Objects.nonNull(states) && !states.isEmpty()) {
            List<State> enumStates = states.stream()
                    .map(String::toUpperCase)
                    .map(State::valueOf)
                    .collect(Collectors.toList());
            BooleanExpression byStates = event.state.in(enumStates);
            builder.and(byStates);
        }

        if (Objects.nonNull(categories) && categories.isEmpty()) {
            BooleanExpression byCategories = event.category.id.in(categories);
            builder.and(byCategories);
        }

        if (Objects.nonNull(rangeStart) && Objects.nonNull(rangeEnd)) {
            checkStartTime(rangeStart, rangeEnd);
            BooleanExpression byEventDate = event.eventDate.between(rangeStart, rangeEnd);
            builder.and(byEventDate);
        }

        List<EventFullDto> eventFullDtoList = eventMapper.toDtoList(eventRepository.findAll(builder, pageable).getContent());

        List<Long> eventsIds = getEventIds(eventFullDtoList);

        List<ParticipationRequestDto> participationRequests = participationMapper.toListDto(
                participationRepository.findAllByEventIdInAndStatus(eventsIds, ParticipationRequestStatus.CONFIRMED));

        for (EventFullDto eventFullDto : eventFullDtoList) {
            long confirmedRequestsCount = participationRequests.stream()
                    .filter(x -> Objects.equals(eventFullDto.getId(), x.getEvent()))
                    .count();
            eventFullDto.setConfirmedRequests((int) confirmedRequestsCount);
        }

        return eventFullDtoList;
    }





    private void checkEventDateUpdate(String time) {
        if (Objects.nonNull(time)) {
            LocalDateTime dateTime = LocalDateTime.parse(time, DateTimeFormatter.ofPattern(DATE_TIME_FORMATTER));
            if (dateTime.isBefore(LocalDateTime.now())) {
                throw new BadRequestException("You cannot change the date to one that has already occurred");
            }
        }
    }


    private void checkEventAlreadyPublished(EventFullDto eventFullDto, UpdateEventAdminRequest updateEventUserRequest) {
        if (updateEventUserRequest.getStateAction() == PUBLISH_EVENT) {
            if (Objects.requireNonNull(eventFullDto.getState()) == PUBLISHED ||
                    Objects.requireNonNull(eventFullDto.getState()) == CANCELED) {
                throw new ConflictException("Event has already published");
            }
        }

    }

    private void checkEventIsPublished(EventFullDto eventFullDto, UpdateEventAdminRequest updateEventAdminRequest) {
        if (Objects.requireNonNull(eventFullDto.getState()) == PUBLISHED &&
                updateEventAdminRequest.getStateAction() == REJECT_EVENT) {
            throw new ConflictException(String.format("Event with id %s already been published at the moment", eventFullDto.getId()));
        }
    }

    private List<Long> getEventIds(List<EventFullDto> eventFullDtoArrayList) {

        return eventFullDtoArrayList.stream()
                .map(EventFullDto::getId)
                .collect(Collectors.toList());
    }

    private EventFullDto findEvenFullDtoById(Long eventId) {

        return eventMapper.toFullDto(eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id %s not found", eventId))));

    }

    private void checkStartTime(LocalDateTime rangeStart, LocalDateTime rangeEnd) {

        if (rangeStart.isAfter(rangeEnd)) {
            throw new BadRequestException("Start cannot be later than the end");
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
}