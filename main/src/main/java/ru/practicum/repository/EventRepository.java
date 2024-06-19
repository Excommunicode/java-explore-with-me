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

    List<Event> findAllByInitiator_Id(Long userId, Pageable pageable);

    Set<Event> findAllByIdIn(Set<Long> events);

    Optional<Event> findByIdAndState(Long id, State state);

    boolean existsByCategory_Id(Long id);

    @Query(nativeQuery = true,
            value = "SELECT * " +
                    "FROM events e " +
                    "WHERE e.state in :states " +
                    "AND (e.event_date BETWEEN :rangeStart AND :rangeEnd) " +
                    "ORDER BY e.id DESC " +
                    "LIMIT :size " +
                    "OFFSET :from")
    List<Event> findAllByStateAndEventDate(List<String> states, LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                           Integer from, Integer size);

    @Query(nativeQuery = true,
            value = "SELECT * " +
                    "FROM events e " +
                    "WHERE e.initiator_id IN :users " +
                    "AND e.category_id IN :categories " +
                    "ORDER BY e.id DESC " +
                    "LIMIT :size " +
                    "OFFSET :from")
    List<Event> findAllByInitiator_IdInAndCategory(List<Long> users, List<Long> categories, Integer from, Integer size);


    @Modifying
    @Query(nativeQuery = true,
            value = "UPDATE events SET confirmed_requests = confirmed_requests + :countConfirmedRequests WHERE id = :eventId")
    void updateByIdInAndConfirmedRequests(Long eventId, int countConfirmedRequests);
}