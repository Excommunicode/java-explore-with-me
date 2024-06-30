package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.model.Rating;

import java.util.List;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    @Modifying
    @Query(nativeQuery = true,
            value = "UPDATE ratings set assessment = :assessment WHERE id = :ratingId")
    int updateRatingByIdAndAssessment(Long ratingId, int assessment);

    List<Rating> findAllByEventIdAndAssessmentNotNull(Long eventId);

    List<Rating> findAllByEventIdInAndAssessmentIsNotNull(List<Long> eventsIds);
}
