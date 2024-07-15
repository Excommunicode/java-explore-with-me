package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.rating.NewRatingDto;
import ru.practicum.dto.rating.RatingDto;
import ru.practicum.service.api.RatingPrivateService;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users/{userId}/rating")
public class RatingPrivateController {
    private final RatingPrivateService ratingPrivateService;

    @PostMapping("/{eventId}")
    @ResponseStatus(HttpStatus.CREATED)
    public RatingDto addRatingDto(@PathVariable Long userId, @PathVariable Long eventId,
                                  @Valid @RequestBody NewRatingDto newRatingDto) {
        return ratingPrivateService.addRatingDto(userId, eventId, newRatingDto);
    }

    @PatchMapping("/{ratingId}")
    public RatingDto updateRatingDto(@PathVariable Long userId, @PathVariable Long ratingId,
                                     @Valid @RequestBody NewRatingDto newRatingDto) {
        return ratingPrivateService.updateRatingDto(userId, ratingId, newRatingDto);
    }

    @DeleteMapping("/{ratingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRatingDto(@PathVariable Long userId, @PathVariable Long ratingId) {
        ratingPrivateService.deleteRatingDto(userId, ratingId);
    }
}