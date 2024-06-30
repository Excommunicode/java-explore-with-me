package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.enums.CommentSort;
import ru.practicum.enums.EventSort;
import ru.practicum.service.impl.CommentPublicService;
import ru.practicum.service.impl.EventPublicService;
import ru.practicum.service.impl.RatingPublicService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
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
    private final CommentPublicService commentPublicService;
    private final RatingPublicService ratingPublicService;

    @GetMapping
    public List<EventFullDto> getPublicEventDto(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) boolean paid,
            @RequestParam(required = false) @DateTimeFormat(pattern = DATE_TIME_FORMATTER) LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = DATE_TIME_FORMATTER) LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "false") Boolean onlyAvailable,
            @PositiveOrZero @RequestParam(defaultValue = INITIAL_X) int from,
            @Positive @RequestParam(defaultValue = LIMIT) int size,
            @RequestParam(required = false) EventSort sort,
            HttpServletRequest httpServletRequest) {
        return eventPublicService.getEventsDto(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size, httpServletRequest);
    }

    @GetMapping("/{id}")
    public EventFullDto getByIdPublic(@PathVariable Long id, HttpServletRequest httpServletRequest) {
        return eventPublicService.getEventByIdPublic(id, httpServletRequest);
    }

    @GetMapping("/{eventId}/comments")
    public List<CommentDto> getAllCommentsDto(@PathVariable Long eventId,
                                              @RequestParam(required = false) CommentSort commentSort,
                                              @PositiveOrZero @RequestParam(defaultValue = INITIAL_X) int from,
                                              @Positive @RequestParam(defaultValue = LIMIT) int size) {
        return commentPublicService.findCommentsDtoById(eventId, commentSort, from, size);
    }

    @GetMapping("/{eventId}/comments/count")
    public long countComments(@PathVariable Long eventId) {
        return commentPublicService.countCommentsByEventId(eventId);
    }

    @GetMapping("/ratings/{eventId}/avg-assessment")
    public double getAvgAssessment(@PathVariable Long eventId) {
        return ratingPublicService.getAvgAssessment(eventId);
    }
}