package ru.practicum.service.impl;

import ru.practicum.dto.event.EventFullDto;
import ru.practicum.enums.EventSort;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

public interface EventPublicService {
    /**
     * Searches for events based on various criteria including text search, category filters, payment status,
     * time range, availability, and sorting preferences. This method also supports pagination.
     *
     * @param text               the text query to filter events (can be part of event title or description)
     * @param categories         a list of category IDs to filter the events
     * @param paid               boolean indicating if only paid events should be returned (true for paid, false for free)
     * @param rangeStart         the start datetime of the event range to filter
     * @param rangeEnd           the end datetime of the event range to filter
     * @param onlyAvailable      boolean indicating if only currently available events should be returned
     * @param sort               the sorting criteria for the events
     * @param from               the starting index from which to retrieve events
     * @param size               the maximum number of events to retrieve
     * @param httpServletRequest the HTTP request context used for additional configurations or authentications
     * @return a list of EventFullDto matching the specified criteria
     */
    List<EventFullDto> getEventsDto(String text, List<Long> categories,
                                    boolean paid, LocalDateTime rangeStart,
                                    LocalDateTime rangeEnd,
                                    boolean onlyAvailable,
                                    EventSort sort, int from,
                                    int size, HttpServletRequest httpServletRequest);

    /**
     * Retrieves a single event by its ID for public viewing, considering any necessary context from the HTTP request.
     *
     * @param id                 the ID of the event to be retrieved
     * @param httpServletRequest the HTTP request context used for additional configurations or authentications
     * @return the EventFullDto containing detailed information about the event
     */
    EventFullDto getEventByIdPublic(Long id, HttpServletRequest httpServletRequest);
}