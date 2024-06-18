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
                                                @RequestParam(defaultValue = "0", required = false) Integer from,
                                                @RequestParam(defaultValue = "10", required = false) Integer size,
                                                @RequestParam(required = false) EventSort sort,
                                                HttpServletRequest httpServletRequest) {

        System.err.println("text: " + text);
        System.err.println("categories: " + categories);
        System.err.println("paid: " + paid);
        System.err.println("rangeStart: " + rangeStart);
        System.err.println("rangeEnd: " + rangeEnd);
        System.err.println("onlyAvailable: " + onlyAvailable);
        System.err.println("from: " + from);
        System.err.println("size: " + size);
        System.err.println("sort: " + sort);
        return eventPublicService.getEventsDto(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size, httpServletRequest);
    }

    @GetMapping("/{id}")
    public EventFullDto getByIdPublic(@PathVariable Long id, HttpServletRequest httpServletRequest) {
        System.err.println("Fetching booking by ID: " + id);
        return eventPublicService.getEventByIdPublic(id, httpServletRequest);
    }
}
