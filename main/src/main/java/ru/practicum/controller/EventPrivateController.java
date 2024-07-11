package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.event.UpdateEventUserRequest;
import ru.practicum.dto.event.UpdateEventUserRequestOutput;
import ru.practicum.dto.registration.EventRequestStatusUpdateRequest;
import ru.practicum.dto.registration.EventRequestStatusUpdateResult;
import ru.practicum.dto.registration.ParticipationRequestDto;
import ru.practicum.service.api.EventPrivateService;
import ru.practicum.service.api.ParticipationPrivateService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

import static ru.practicum.constant.UserConstant.INITIAL_X;
import static ru.practicum.constant.UserConstant.LIMIT;

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
    public List<EventFullDto> getEventsByUserId(@PathVariable Long userId,
                                                @PositiveOrZero @RequestParam(defaultValue = INITIAL_X) int from,
                                                @Positive @RequestParam(defaultValue = LIMIT) int size) {
        return eventPrivateService.getEventByUserId(userId, from, size);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEventForVerification(@PathVariable Long userId, @PathVariable Long eventId, HttpServletRequest httpServletRequest) {
        return eventPrivateService.getEventForVerificationUser(userId, eventId, httpServletRequest); // TODO
    }

    @PatchMapping("/{eventId}")
    public UpdateEventUserRequestOutput updateEvent(@PathVariable Long userId, @PathVariable Long eventId,
                                                    @Valid @RequestBody UpdateEventUserRequest updateEventUserReport) {
//        System.err.println(updateEventUserReport);
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
