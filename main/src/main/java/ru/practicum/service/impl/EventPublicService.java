package ru.practicum.service.impl;

import ru.practicum.dto.event.EventFullDto;
import ru.practicum.enums.EventSort;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

public interface EventPublicService {
    List<EventFullDto> getEventsDto(String text, List<Long> categories,
                                    boolean paid, LocalDateTime rangeStart,
                                    LocalDateTime rangeEnd,
                                    boolean onlyAvailable,
                                    EventSort sort, int from,
                                    int size, HttpServletRequest httpServletRequest);

    EventFullDto getEventByIdPublic(Long id, HttpServletRequest httpServletRequest);
}