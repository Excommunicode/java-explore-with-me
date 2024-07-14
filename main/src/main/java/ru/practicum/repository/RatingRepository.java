package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.model.Rating;

import java.util.List;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    @Modifying
    @Query("UPDATE Rating e SET e.assessment = :assessment WHERE e.id = :ratingId")
    int updateRatingByIdAndAssessment(Long ratingId, int assessment);

    @Query("SELECT r " +
            "FROM Rating r " +
            "JOIN FETCH r.event e " +
            "WHERE e.id = :eventId " +
            "AND r.assessment IS NOT NULL")
    List<Rating> findAllByEventIdAndAssessmentNotNull(Long eventId);

    @Query("SELECT r " +
            "FROM Rating r " +
            "JOIN FETCH r.event e " +
            "WHERE e.id IN :eventsIds " +
            "AND r.assessment IS NOT NULL")
    List<Rating> findAllByEventIdInAndAssessmentIsNotNull(List<Long> eventsIds);
}