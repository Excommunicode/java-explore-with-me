package ru.practicum.service.api;

import ru.practicum.dto.registration.EventRequestStatusUpdateRequest;
import ru.practicum.dto.registration.EventRequestStatusUpdateResult;
import ru.practicum.dto.registration.ParticipationRequestDto;

import java.util.List;

public interface ParticipationPrivateService {
    /**
     * Submits a new participation request for an event by a user.
     *
     * @param userId the ID of the user making the request
     * @param eventId the ID of the event for which the request is made
     * @return the ParticipationRequestDto containing details of the newly created participation request
     */
    ParticipationRequestDto addRequest(Long userId, Long eventId);

    /**
     * Retrieves all participation requests made by a specific user across all events.
     *
     * @param userId the ID of the user whose participation requests are being queried
     * @return a list of ParticipationRequestDto detailing each registration request made by the user
     */
    List<ParticipationRequestDto> findInformationAboutUserRegistration(Long userId);

    /**
     * Retrieves all participation requests made by a specific user for a specific event.
     *
     * @param userId the ID of the user
     * @param eventId the ID of the event
     * @return a list of ParticipationRequestDto representing the user's registration requests for the event
     */
    List<ParticipationRequestDto> findRequestRegistration(Long userId, Long eventId);

    /**
     * Changes the status of participation requests for a specific user and event based on the provided update request.
     *
     * @param userId the ID of the user whose requests are to be updated
     * @param eventId the ID of the event associated with the requests
     * @param newRequestsEvent the request containing the new status details for the event requests
     * @return an EventRequestStatusUpdateResult representing the result of the update operation
     */
    EventRequestStatusUpdateResult changeStateRequests(Long userId, Long eventId, EventRequestStatusUpdateRequest newRequestsEvent);

    /**
     * Cancels a participation request for a user.
     *
     * @param userId the ID of the user canceling the request
     * @param requestId the ID of the request to be canceled
     * @return the updated ParticipationRequestDto reflecting the cancellation of the request
     */
    ParticipationRequestDto cancelingYourRequest(Long userId, Long requestId);
}