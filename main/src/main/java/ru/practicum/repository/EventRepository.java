package ru.practicum.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.enums.State;
import ru.practicum.model.Event;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long>, QuerydslPredicateExecutor<Event> {

    /**
     * Retrieves all events initiated by a specific user, with pagination.
     *
     * @param userId   the ID of the user who initiated the events.
     * @param pageable the pagination information (page number, page size, sorting directions).
     * @return a pageable list of {@link Event} objects initiated by the specified user.
     */
    @Query("SELECT e " +
            "FROM Event e " +
            "JOIN FETCH e.initiator i " +
            "WHERE i.id = :userId")
    List<Event> findAllByInitiator_Id(Long userId, Pageable pageable);

    /**
     * Retrieves an event by its ID and state.
     *
     * @param id    the ID of the event.
     * @param state the state of the event.
     * @return an {@link Optional} of {@link Event} if found, or an empty Optional if no event matches the criteria.
     */
    @Query("SELECT e " +
            "FROM Event e " +
            "WHERE e.id = :id " +
            "AND e.state = :state")
    Optional<Event> findByIdAndState(Long id, State state);

    /**
     * Checks if any events exist with a specific category ID.
     *
     * @param id the category ID to check.
     * @return true if any events with the specified category ID exist, otherwise false.
     */
    @Query("SELECT COUNT(e) > 0 " +
            "FROM Event e " +
            "WHERE e.category.id = :id")
    boolean existsByCategory_Id(Long id);

    /**
     * Checks if any events exist with a specific user ID and event ID.
     *
     * @param userId  the ID to check.D
     * @param eventId the ID to check.D
     * @return true if any events with the specified category ID exist, otherwise false.
     */
    @Query("SELECT COUNT(e) > 0 " +
            "FROM Event e " +
            "JOIN e.initiator u " +
            "WHERE e.id = :eventId " +
            "AND u.id = :userId")
    boolean existsByIdAndInitiator_Id(Long eventId, Long userId);

    /**
     * Updates the number of confirmed requests for an event by incrementing them by a specified amount.
     *
     * @param eventId                the ID of the event to update.
     * @param countConfirmedRequests the number to add to the current count of confirmed requests.
     */
    @Modifying
    @Query("UPDATE Event e SET e.confirmedRequests = e.confirmedRequests + :countConfirmedRequests WHERE e.id = :eventId")
    void updateConfirmedRequestsById(Long eventId, int countConfirmedRequests);
}