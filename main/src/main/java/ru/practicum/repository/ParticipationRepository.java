package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.enums.ParticipationRequestStatus;
import ru.practicum.model.ParticipationRequest;

import java.util.List;

public interface ParticipationRepository extends JpaRepository<ParticipationRequest, Long> {

    /**
     * Finds all participation requests initiated by a specific user for a specific event.
     *
     * @param userId  the ID of the user who initiated the event.
     * @param eventId the ID of the event for which participation requests are being searched.
     * @return a list of {@link ParticipationRequest} that meet the criteria.
     */
    List<ParticipationRequest> findAllByEventInitiatorIdAndEventId(Long userId, Long eventId);

    /**
     * Retrieves all participation requests made by a specific requester.
     *
     * @param userId the ID of the requester whose participation requests are to be retrieved.
     * @return a list of {@link ParticipationRequest} associated with the given requester ID.
     */
    List<ParticipationRequest> findAllByRequester_Id(Long userId);

    /**
     * Checks if there is a participation request by a specific requester for a specific event.
     *
     * @param userId  the ID of the requester.
     * @param eventId the ID of the event.
     * @return true if such a request exists, false otherwise.
     */
    boolean existsByRequester_IdAndEvent_id(Long userId, Long eventId);

    /**
     * Finds all participation requests with IDs in the provided list.
     *
     * @param requestIds a list of IDs for the participation requests to retrieve.
     * @return a list of {@link ParticipationRequest} with IDs from the provided list.
     */
    List<ParticipationRequest> findAllByIdIn(List<Long> requestIds);

    /**
     * Finds all participation requests for events in the provided list with a specific status.
     *
     * @param eventIds a list of event IDs to search within.
     * @param status   the status of the participation requests to filter by.
     * @return a list of {@link ParticipationRequest} that meet the search criteria.
     */
    List<ParticipationRequest> findAllByEventIdInAndStatus(List<Long> eventIds, ParticipationRequestStatus status);

    /**
     * Updates the status of a participation request by its ID.
     *
     * @param id     the ID of the participation request to update.
     * @param status the new status to set for the participation request.
     */
    @Modifying
    @Query("UPDATE ParticipationRequest p SET p.status = :status WHERE p.id = :id")
    void updateByIdStatus(Long id, ParticipationRequestStatus status);

    /**
     * Updates the status for multiple participation requests by their IDs.
     *
     * @param participationIds a list of IDs for the participation requests to update.
     * @param status           the new status to apply to the participation requests.
     */
    @Modifying
    @Query("UPDATE ParticipationRequest p SET p.status = :status WHERE p.id IN :participationIds")
    void updateStatusByParticipationIds(@Param("participationIds") List<Long> participationIds, @Param("status") ParticipationRequestStatus status);
}