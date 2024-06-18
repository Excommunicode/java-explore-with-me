package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.UpdateEventAdminRequest;
import ru.practicum.dto.user.UserDto;
import ru.practicum.service.impl.EventAdminService;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.constant.EventConstant.DATE_TIME_FORMATTER;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/admin/events")
public class EventAdminController {
    private final EventAdminService eventAdminService;

    @GetMapping
    public List<EventFullDto> findEventForAdmin(@RequestParam(required = false) List<Long> users,
                                                @RequestParam(required = false) List<String> states,
                                                @RequestParam(required = false) List<Long> categories,
                                                @RequestParam(required = false) //@DateTimeFormat(pattern = DATE_TIME_FORMATTER)
                                                String rangeStart,
                                                @RequestParam(required = false)// @DateTimeFormat(pattern = DATE_TIME_FORMATTER)
                                                String rangeEnd,
                                                @RequestParam(defaultValue = "0") Integer from,
                                                @RequestParam(defaultValue = "10") Integer size) {
        System.err.println("Users: " + users);
        System.err.println("States: " + states);
        System.err.println("Categories: " + categories);
        System.err.println("Range Start: " + rangeStart);
        System.err.println("Range End: " + rangeEnd);
        System.err.println("From: " + from);
        System.err.println("Size: " + size);
        System.err.println("!!!!!!!!!bdweFGYYUEyugUGUy");
        List<EventFullDto> eventForAdmin = eventAdminService.findEventForAdmin(users, states, categories, rangeStart, rangeEnd, from, size);

        return eventForAdmin;
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEventBydAdmin(@PathVariable Long eventId,
                                            @Valid
                                            @RequestBody UpdateEventAdminRequest updateEventAdminRequest) {
        System.err.println("111111111111111111111111111111111111111");
        EventFullDto eventFullDto = eventAdminService.updateEventByAdmin(eventId, updateEventAdminRequest);
        System.err.println("Update event by admin with id " + eventFullDto.getId());
        return eventFullDto;

    }

}
