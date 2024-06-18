package ru.practicum.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.enums.State;
import ru.practicum.model.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    List<Event> findAllByInitiator_Id(Long userId, Pageable pageable);

    @Query(nativeQuery = true,
            value = "SELECT * " +
                    "FROM events e " +
                    "WHERE (LOWER(:title) LIKE LOWER(e.title) " +
                    "OR (e.event_date BETWEEN :rangeStart AND :rangeEnd)) " +
                    "AND e.paid = :paid " +
                    "ORDER BY e.id DESC " +
                    "LIMIT :size " +
                    "OFFSET :from")
    List<Event> findAllByTitleAndEventDateAndPaidOrderByEventDate(String title, LocalDateTime rangeStart,
                                                                  LocalDateTime rangeEnd, boolean paid,
                                                                  Integer from, Integer size);

    @Query(nativeQuery = true,
            value = "SELECT * " +
                    "FROM events e " +
                    "WHERE (LOWER(:title) LIKE LOWER(e.title)   " +
                    "OR (e.event_date BETWEEN :rangeStart AND :rangeEnd)) " +
                    "AND e.paid = :paid " +
                    "ORDER BY e.views DESC " +
                    "LIMIT :size " +
                    "OFFSET :from")
    List<Event> findAllByTitleAndEventDateAndPaidOrderByViews(String title, LocalDateTime rangeStart,
                                                              LocalDateTime rangeEnd, boolean paid,
                                                              Integer from, Integer size);

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


    List<Event> findAllByEventDateBetweenAndPaid(LocalDateTime rangeStart, LocalDateTime rangeEnd, boolean paid, Pageable pageable);

    Set<Event> findAllByIdIn(Set<Long> events);

    List<Event> getAllByIdIn(Set<Long> events);

    Optional<Event> findByIdAndState(Long id, State state);

    @Query(nativeQuery = true,
            value = "SELECT * " +
                    "FROM events e " +
                    "WHERE e.initiator_id IN :users " +
                    "AND e.category_id IN :categories " +
                    "ORDER BY e.id DESC " +
                    "LIMIT :size " +
                    "OFFSET :from")
    List<Event> findAllByInitiator_IdInAndCategory(List<Long> users, List<Long> categories, Integer from, Integer size);

    boolean existsByCategory_Id(Long id);

    boolean existsByInitiator_IdAndId(Long userId, Long eventId);

    @Query(nativeQuery = true,
    value = "SELECT * " +
            "FROM events e " +
            "WHERE e.initiator_id IN :users " +
            "AND e.category_id IN :categories " +
            "AND e.event_date BETWEEN :rangeStart AND :rangeEnd " +
            "AND e.state IN :states " +
            "LIMIT :size " +
            "OFFSET :from")
        List<Event> findAllByInitiator_IdInAndEventDateIsBetweenAndCategory_IdIn(List<Long> users,
                                                                             List<Long> categories,
                                                                             LocalDateTime rangeStart,
                                                                             LocalDateTime rangeEnd,
                                                                             Integer from, Integer size,
                                                                             List<String> states);

}