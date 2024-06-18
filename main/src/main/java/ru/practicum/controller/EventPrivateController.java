package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.event.UpdateEventUserRequest;
import ru.practicum.dto.event.UpdateEventUserRequestOutput;
import ru.practicum.dto.registration.ParticipationRequestDto;
import ru.practicum.dto.registration.EventRequestStatusUpdateResult;
import ru.practicum.dto.registration.EventRequestStatusUpdateRequest;
import ru.practicum.service.impl.EventPrivateService;
import ru.practicum.service.impl.ParticipationPrivateService;

import javax.validation.Valid;
import java.util.List;

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
        System.err.println("Creating event for user with ID: " + userId);
        EventFullDto event = eventPrivateService.addEventDto(newEventDto, userId);
        System.err.println("Event created with ID: " + event.getId());
        return event;
    }

    @GetMapping
    public List<EventFullDto> getEventsByUserId(@PathVariable Long userId, @RequestParam(defaultValue = "0") Integer from,
                                                @RequestParam(defaultValue = "10") Integer size) {
        System.err.println("Fetching events for user with ID: " + userId + " from index: " + from + " with size: " + size);
        List<EventFullDto> events = eventPrivateService.getEventByUserId(userId, from, size);
        System.err.println("Fetched " + events.size() + " events for user with ID: " + userId);
        return events;
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEventForVerification(@PathVariable Long userId, @PathVariable Long eventId) {
        System.err.println("Fetching event with ID: " + eventId + " for user with ID: " + userId);
        EventFullDto event = eventPrivateService.getEventForVerificationUser(userId, eventId);
        System.err.println("Fetched event: " + event);
        return event;
    }

    @PatchMapping("/{eventId}")
    public UpdateEventUserRequestOutput updateEvent(@PathVariable Long userId, @PathVariable Long eventId,
                                                    @Valid @RequestBody UpdateEventUserRequest updateEventUserReport) {
        System.err.println("Updating event with ID: " + eventId + " for user with ID: " + userId);
        System.err.println("state action: " + updateEventUserReport.getStateAction());
        UpdateEventUserRequestOutput updatedEvent = eventPrivateService.updateEvent(userId, eventId, updateEventUserReport);
        System.err.println("Updated event: " + updatedEvent);
        return updatedEvent;
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> findRequestRegistration(@PathVariable Long userId, @PathVariable Long eventId) {
        System.out.println("userId: " + userId + " eventId: " + eventId);
        return eventRegistrationService.findRequestRegistration(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult changeStateRequests(@PathVariable Long userId, @PathVariable Long eventId,
                                                              @RequestBody EventRequestStatusUpdateRequest newRequestsEvent) {
        System.err.println("Bebraaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        for (Long requestId : newRequestsEvent.getRequestIds()) {
            System.err.println(requestId);
        }
        System.err.println(newRequestsEvent.getStatus());
        return eventRegistrationService.changeStateRequests(userId, eventId, newRequestsEvent);
    }

}
