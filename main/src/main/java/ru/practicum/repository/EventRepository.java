package ru.practicum.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.enums.State;
import ru.practicum.model.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    /**
     * Retrieves all events initiated by a specific user, with pagination.
     *
     * @param userId   the ID of the user who initiated the events.
     * @param pageable the pagination information (page number, page size, sorting directions).
     * @return a pageable list of {@link Event} objects initiated by the specified user.
     */
    List<Event> findAllByInitiator_Id(Long userId, Pageable pageable);

    /**
     * Finds all events by their IDs from a set.
     *
     * @param events a set of event IDs to retrieve.
     * @return a set of {@link Event} objects corresponding to the provided IDs.
     */
    Set<Event> findAllByIdIn(Set<Long> events);

    /**
     * Retrieves an event by its ID and state.
     *
     * @param id    the ID of the event.
     * @param state the state of the event.
     * @return an {@link Optional} of {@link Event} if found, or an empty Optional if no event matches the criteria.
     */
    Optional<Event> findByIdAndState(Long id, State state);

    /**
     * Checks if any events exist with a specific category ID.
     *
     * @param id the category ID to check.
     * @return true if any events with the specified category ID exist, otherwise false.
     */
    boolean existsByCategory_Id(Long id);

    /**
     * Finds all events that match a list of states within a specified date range, ordered by their IDs in descending order, with pagination.
     *
     * @param states     the list of states to filter the events.
     * @param rangeStart the start of the date range.
     * @param rangeEnd   the end of the date range.
     * @param pageable   the pagination information.
     * @return a pageable list of {@link Event} that meet the criteria.
     */
    @Query(nativeQuery = true,
            value = "SELECT * " +
                    "FROM events e " +
                    "WHERE e.state in :states " +
                    "AND (e.event_date BETWEEN :rangeStart AND :rangeEnd) " +
                    "ORDER BY e.id DESC")
    List<Event> findAllByStateAndEventDate(List<String> states, LocalDateTime rangeStart, LocalDateTime rangeEnd, Pageable pageable);

    /**
     * Retrieves all events initiated by users in a given list and falling within specified categories, with pagination.
     *
     * @param users      a list of user IDs who initiated the events.
     * @param categories a list of category IDs to filter the events.
     * @param pageable   the pagination information.
     * @return a pageable list of {@link Event} that match the specified initiators and categories.
     */
    @Query(nativeQuery = true, value = "SELECT * " +
            "FROM events e " +
            "WHERE e.initiator_id IN :users " +
            "AND e.category_id IN :categories " +
            "ORDER BY e.id DESC")
    List<Event> findAllByInitiator_IdInAndCategory(List<Long> users, List<Long> categories, Pageable pageable);

    /**
     * Updates the number of confirmed requests for an event by incrementing them by a specified amount.
     *
     * @param eventId                the ID of the event to update.
     * @param countConfirmedRequests the number to add to the current count of confirmed requests.
     */
    @Modifying
    @Query(nativeQuery = true,
            value = "UPDATE events SET confirmed_requests = confirmed_requests + :countConfirmedRequests WHERE id = :eventId")
    void updateByIdInAndConfirmedRequests(Long eventId, int countConfirmedRequests);


}