package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.rating.NewRatingDto;
import ru.practicum.dto.rating.RatingDto;
import ru.practicum.exceptiion.ConflictException;
import ru.practicum.exceptiion.NotFoundException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.mapper.RatingMapper;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.RatingRepository;
import ru.practicum.service.impl.RatingPrivateService;
import ru.practicum.service.impl.RatingPublicService;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(
        readOnly = true,
        isolation = Isolation.REPEATABLE_READ,
        propagation = Propagation.REQUIRED
)
public class RatingServiceImpl implements RatingPrivateService, RatingPublicService {
    private final RatingRepository ratingRepository;
    private final RatingMapper ratingMapper;
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;


    @Transactional
    @Override
    public RatingDto addRatingDto(Long userId, Long eventId, NewRatingDto newRatingDto) {
        log.debug("Adding a new rating for event with ID: {} from user with ID: {}", eventId, userId);

        EventFullDto eventFullDto = findEventById(eventId);
        checkIsInitiator(userId, eventFullDto.getInitiator().getId());

        RatingDto ratingDto = ratingMapper.toDto(ratingRepository.save(ratingMapper.toModel(userId, eventId, newRatingDto)));
        log.info("Rating with ID: {} successfully added", ratingDto.getId());
        return ratingDto;
    }

    @Transactional
    @Override
    public RatingDto updateRatingDto(Long userId, Long ratingId, NewRatingDto newRatingDto) {
        log.debug("Updating Rating with ID: {}", ratingId);

        RatingDto ratingDto = findRatingDto(ratingId);
        checkIsAfter(userId, ratingDto.getUserId());

        ratingRepository.updateRatingByIdAndAssessment(ratingId, newRatingDto.getAssessment());

        RatingDto ratingDtoAfterUpdate = ratingMapper.toDtoAfterUpdate(ratingId, userId, newRatingDto);

        log.info("Update completed for Rating with ID: {}", ratingDtoAfterUpdate);
        return ratingDtoAfterUpdate;
    }

    @Transactional
    @Override
    public void deleteRatingDto(Long userId, Long ratingId) {
        RatingDto ratingDto = findRatingDto(userId);
        checkIsAfter(userId, ratingDto.getUserId());
        ratingRepository.deleteById(ratingId);
    }

    @Override
    public double getAvgAssessment(Long eventId) {
        log.debug("Entering getAvgAssessment with ratingId: {}", eventId);
        List<RatingDto> ratingDtoList = ratingMapper.toDtoList(
                ratingRepository.findAllByEventIdAndAssessmentNotNull(eventId));

        if (ratingDtoList.isEmpty()) {
            log.warn("No ratings found for ratingId: {}", eventId);
            return 0.0;
        }

        double avg = ratingDtoList.stream()
                .mapToDouble(RatingDto::getAssessment)
                .average()
                .orElse(0.0);
        log.info("Calculated average assessment for ratingId {}: {}", eventId, avg);
        return avg;
    }

    private RatingDto findRatingDto(Long ratingId) {
        return ratingMapper.toDto(ratingRepository.findById(ratingId)
                .orElseThrow(() -> new NotFoundException(String.format("Rating with id %s not found", ratingId))));
    }

    private EventFullDto findEventById(Long eventId) {
        return eventMapper.toFullDto(eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id %s not found", eventId))));
    }

    private void checkIsAfter(Long userId, Long afterId) {
        if (!Objects.equals(userId, afterId)) {
            throw new ConflictException(String.format("User with id %s is not after rating", userId));
        }
    }

    private void checkIsInitiator(Long userId, Long initiatorId) {
        if (Objects.equals(userId, initiatorId)) {
            throw new ConflictException(String.format(
                    "initiator of the event with %s identifier tried to like himself", initiatorId));
        }
    }
}
