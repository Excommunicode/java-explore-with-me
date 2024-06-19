package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.UpdateEventAdminRequest;
import ru.practicum.service.impl.EventAdminService;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.constant.EventConstant.DATE_TIME_FORMATTER;
import static ru.practicum.constant.UserConstant.INITIAL_X;
import static ru.practicum.constant.UserConstant.LIMIT;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/admin/events")
public class EventAdminController {
    private final EventAdminService eventAdminService;

    @GetMapping
    public List<EventFullDto> findEventForAdmin(@RequestParam(required = false) List<Long> users,
                                                @RequestParam(required = false) List<String> states,
                                                @RequestParam(required = false) List<Long> categories,
                                                @RequestParam(required = false) @DateTimeFormat(pattern = DATE_TIME_FORMATTER) LocalDateTime rangeStart,
                                                @RequestParam(required = false) @DateTimeFormat(pattern = DATE_TIME_FORMATTER) LocalDateTime rangeEnd,
                                                @RequestParam(defaultValue = INITIAL_X) int from,
                                                @RequestParam(defaultValue = LIMIT) int size) {
        return eventAdminService.findEventForAdmin(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEventBydAdmin(@PathVariable Long eventId,
                                            @Valid @RequestBody UpdateEventAdminRequest updateEventAdminRequest) {

        return eventAdminService.updateEventByAdmin(eventId, updateEventAdminRequest);

    }

}
