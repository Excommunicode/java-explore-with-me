package ru.practicum.service;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.EndpointDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.rating.RatingDto;
import ru.practicum.enums.EventSort;
import ru.practicum.exceptiion.BadRequestException;
import ru.practicum.exceptiion.NotFoundException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.mapper.RatingMapper;
import ru.practicum.model.QEvent;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.RatingRepository;
import ru.practicum.service.api.EventPublicService;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static ru.practicum.constant.EventConstant.DATE_TIME_FORMATTER;
import static ru.practicum.constant.EventConstant.EVM_SERVICE;
import static ru.practicum.enums.State.PUBLISHED;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventPublicServiceImpl implements EventPublicService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final RatingRepository ratingRepository;
    private final RatingMapper ratingMapper;
    private final StatsClient statsClient;


    @Override
    public List<EventFullDto> getEventsDto(String text, List<Long> categories, boolean paid, LocalDateTime rangeStart, LocalDateTime rangeEnd, boolean onlyAvailable, EventSort sort, int from, int size, HttpServletRequest httpServletRequest) {
        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size, Sort.by(Sort.Direction.DESC, "id"));
        BooleanBuilder builder = new BooleanBuilder();
        QEvent event = QEvent.event;

        if (Objects.nonNull(text) && !text.trim().isEmpty()) {
            String textLowerCase = text.toLowerCase();
            BooleanExpression byAnnotation = event.annotation.toLowerCase().contains(textLowerCase);
            BooleanExpression byText = event.title.toLowerCase().contains(textLowerCase);
            builder.and(byAnnotation).or(byText);
        }

        BooleanExpression byEventDate;
        if (Objects.nonNull(rangeStart) && Objects.nonNull(rangeEnd)) {
            checkStartTime(rangeStart, rangeEnd);
            byEventDate = event.eventDate.between(rangeStart, rangeEnd);
        } else {
            byEventDate = event.eventDate.after(LocalDateTime.now());
        }
        builder.and(byEventDate);

        if (Objects.nonNull(categories) && !categories.isEmpty()) {
            BooleanExpression byCategories = event.category.id.in(categories);
            builder.and(byCategories);
        }

        List<EventFullDto> eventFullDtoAfterIterable = eventMapper.toEventFullDtoAfterIterable(eventRepository.findAll(builder, pageable));
        recordRequestStats(httpServletRequest);
        List<EventFullDto> eventFullDtoList = setAverageRatings(sort, eventFullDtoAfterIterable, getEventIds(eventFullDtoAfterIterable));
        return eventFullDtoList.isEmpty() ? eventFullDtoAfterIterable : eventFullDtoList;
    }

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

    private void checkStartTime(LocalDateTime rangeStart, LocalDateTime rangeEnd) {

        if (rangeStart.isAfter(rangeEnd)) {
            throw new BadRequestException("Start cannot be later than the end");
        }

    }

    private List<Long> getEventIds(List<EventFullDto> eventFullDtoArrayList) {

        return eventFullDtoArrayList.stream()
                .map(EventFullDto::getId)
                .collect(Collectors.toList());
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
}
