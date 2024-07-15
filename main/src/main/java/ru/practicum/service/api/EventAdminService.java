package ru.practicum.service.api;

import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.UpdateEventAdminRequest;

import java.time.LocalDateTime;
import java.util.List;

public interface EventAdminService {

    /**
     * Updates an event by an administrator using provided details.
     *
     * @param eventId the ID of the event to be updated
     * @param updateEventAdminRequest the request containing the new details for updating the event
     * @return the updated EventFullDto containing the modified event details
     */
    EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateEventAdminRequest);

    /**
     * Finds events based on administrative criteria, including filtering by user IDs, event states,
     * categories, and a specific time range. Supports pagination.
     *
     * @param users a list of user IDs to filter the events
     * @param states a list of event states to filter the events
     * @param categories a list of category IDs to filter the events
     * @param rangeStart the start of the time range for filtering events
     * @param rangeEnd the end of the time range for filtering events
     * @param from the starting index from which to retrieve events
     * @param size the maximum number of events to retrieve
     * @return a list of EventFullDto containing the filtered events
     */
    List<EventFullDto> findEventForAdmin(List<Long> users, List<String> states, List<Long> categories,
                                         LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size);
}
