package ru.practicum.service.api;

import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.event.UpdateEventUserRequest;
import ru.practicum.dto.event.UpdateEventUserRequestOutput;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface EventPrivateService {
    /**
     * Adds a new event for a specific user based on the provided data.
     *
     * @param newEventDto the data transfer object containing the details of the new event
     * @param userId      the ID of the user creating the event
     * @return the EventFullDto containing details of the newly created event
     */
    EventFullDto addEventDto(NewEventDto newEventDto, Long userId);

    /**
     * Retrieves a list of events created by a specific user, with pagination support.
     *
     * @param userId the ID of the user whose events are to be retrieved
     * @param from   the starting index from which to retrieve events
     * @param size   the maximum number of events to retrieve
     * @return a list of EventFullDto containing the user's events
     */
    List<EventFullDto> getEventByUserId(Long userId, int from, int size);

    /**
     * Retrieves a specific event for verification by a user.
     *
     * @param userId  the ID of the user verifying the event
     * @param eventId the ID of the event to be verified
     * @return the EventFullDto containing the event details for verification
     */
    EventFullDto getEventForVerificationUser(Long userId, Long eventId, HttpServletRequest httpServletRequest);

    /**
     * Updates an event by a user with new details provided in the request.
     *
     * @param userId                 the ID of the user updating the event
     * @param eventId                the ID of the event to be updated
     * @param updateEventUserRequest the request containing the new details for the event
     * @return an UpdateEventUserRequestOutput containing the result of the update operation
     */
    UpdateEventUserRequestOutput updateEvent(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest);
}