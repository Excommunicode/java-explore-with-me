package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.enums.EventSort;
import ru.practicum.service.impl.EventPublicService;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.constant.EventConstant.DATE_TIME_FORMATTER;
import static ru.practicum.constant.UserConstant.INITIAL_X;
import static ru.practicum.constant.UserConstant.LIMIT;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/events")
public class EventPublicController {
    private final EventPublicService eventPublicService;

    @GetMapping
    public List<EventFullDto> getPublicEventDto(@RequestParam(required = false) String text,
                                                @RequestParam(required = false) List<Long> categories,
                                                @RequestParam(required = false) boolean paid,
                                                @RequestParam(required = false) @DateTimeFormat(pattern = DATE_TIME_FORMATTER) LocalDateTime rangeStart,
                                                @RequestParam(required = false) @DateTimeFormat(pattern = DATE_TIME_FORMATTER) LocalDateTime rangeEnd,
                                                @RequestParam(defaultValue = "false", required = false) Boolean onlyAvailable,
                                                @RequestParam(defaultValue = INITIAL_X, required = false) int from,
                                                @RequestParam(defaultValue = LIMIT, required = false) int size,
                                                @RequestParam(required = false) EventSort sort,
                                                HttpServletRequest httpServletRequest) {

        return eventPublicService.getEventsDto(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size, httpServletRequest);
    }

    @GetMapping("/{id}")
    public EventFullDto getByIdPublic(@PathVariable Long id, HttpServletRequest httpServletRequest) {
        return eventPublicService.getEventByIdPublic(id, httpServletRequest);
    }
}
