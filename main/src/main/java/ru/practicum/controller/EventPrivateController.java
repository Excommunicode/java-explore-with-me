package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.event.UpdateEventUserRequest;
import ru.practicum.dto.event.UpdateEventUserRequestOutput;
import ru.practicum.dto.registration.EventRequestStatusUpdateRequest;
import ru.practicum.dto.registration.EventRequestStatusUpdateResult;
import ru.practicum.dto.registration.ParticipationRequestDto;
import ru.practicum.service.impl.EventPrivateService;
import ru.practicum.service.impl.ParticipationPrivateService;

import javax.validation.Valid;
import java.util.List;

import static ru.practicum.constant.UserConstant.INITIAL_X;
import static ru.practicum.constant.UserConstant.LIMIT;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users/{userId}/events")
public class EventPrivateController {
    private final EventPrivateService eventPrivateService;
    private final ParticipationPrivateService eventRegistrationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto createEvent(@PathVariable Long userId, @Valid @RequestBody NewEventDto newEventDto) {
        return eventPrivateService.addEventDto(newEventDto, userId);
    }

    @GetMapping
    public List<EventFullDto> getEventsByUserId(@PathVariable Long userId, @RequestParam(defaultValue = INITIAL_X) int from,
                                                @RequestParam(defaultValue = LIMIT) int size) {

        return eventPrivateService.getEventByUserId(userId, from, size);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEventForVerification(@PathVariable Long userId, @PathVariable Long eventId) {

        return eventPrivateService.getEventForVerificationUser(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public UpdateEventUserRequestOutput updateEvent(@PathVariable Long userId, @PathVariable Long eventId,
                                                    @Valid @RequestBody UpdateEventUserRequest updateEventUserReport) {
        return eventPrivateService.updateEvent(userId, eventId, updateEventUserReport);
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> findRequestRegistration(@PathVariable Long userId, @PathVariable Long eventId) {
        return eventRegistrationService.findRequestRegistration(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult changeStateRequests(@PathVariable Long userId, @PathVariable Long eventId,
                                                              @RequestBody EventRequestStatusUpdateRequest newRequestsEvent) {
        return eventRegistrationService.changeStateRequests(userId, eventId, newRequestsEvent);
    }

}
