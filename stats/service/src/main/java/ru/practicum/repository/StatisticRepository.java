package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.model.Statistic;
import ru.practicum.model.StatisticProjection;

import java.time.LocalDateTime;
import java.util.List;

public interface StatisticRepository extends JpaRepository<Statistic, Long> {
    /**
     * Finds all statistics projections by timestamp between two given dates.
     *
     * @param start The start date and time.
     * @param end   The end date and time.
     * @return A list of statistic projections.
     */
    @Query("SELECT s.app, s.uri " +
            "FROM Statistic s  " +
            "WHERE s.timestamp BETWEEN :start AND :end " +
            "GROUP BY s.app, s.uri")
    List<StatisticProjection> findAllByTimestampBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Retrieves a list of StatisticProjection objects based on the given start and end timestamps.
     *
     * @param start the start timestamp
     * @param end   the end timestamp
     * @return a list of StatisticProjection objects
     */
    List<StatisticProjection> findAllStatisticProjectionByTimestampBetween(LocalDateTime start,
                                                                           LocalDateTime end);

    /**
     * Find all statistics projections by timestamp between a start and end time and matching URIs.
     *
     * @param start The start time
     * @param end   The end time
     * @param uri   A List of URIs to match
     * @return A List of StatisticProjection objects
     */
    @Query("SELECT s.app, s.uri, s.ip " +
            "FROM Statistic s " +
            "WHERE (s.timestamp BETWEEN :start AND :end) " +
            "AND s.uri IN :uri " +
            "GROUP BY s.app, s.uri, s.ip")
    List<StatisticProjection> findAllStatisticProjectionByTimestampBetweenAndUri(LocalDateTime start,
                                                                                 LocalDateTime end,
                                                                                 List<String> uri);

    /**
     * Finds all StatisticProjection objects by timestamp range and a list of URIs.
     *
     * @param start the start of the timestamp range
     * @param end   the end of the timestamp range
     * @param uri   the list of URIs to filter by
     * @return a List of StatisticProjection objects that match the criteria
     */
    List<StatisticProjection> findAllStatisticProjectionByTimestampBetweenAndUriIn(LocalDateTime start,
                                                                                   LocalDateTime end,
                                                                                   List<String> uri);
}