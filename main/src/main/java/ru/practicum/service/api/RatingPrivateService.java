package ru.practicum.service.api;

import ru.practicum.dto.rating.NewRatingDto;
import ru.practicum.dto.rating.RatingDto;

public interface RatingPrivateService {
    RatingDto addRatingDto(Long userId, Long eventId, NewRatingDto ratingDto);

    RatingDto updateRatingDto(Long userId, Long ratingId, NewRatingDto newRatingDto);

    void deleteRatingDto(Long userId, Long ratingId);
}