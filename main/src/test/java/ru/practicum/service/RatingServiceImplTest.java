package ru.practicum.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.rating.NewRatingDto;
import ru.practicum.dto.rating.RatingDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.exceptiion.ConflictException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.mapper.RatingMapper;
import ru.practicum.model.Event;
import ru.practicum.model.Rating;
import ru.practicum.model.User;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.RatingRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RatingServiceImplTest {
    @InjectMocks
    private RatingServiceImpl ratingService;
    @Mock
    private RatingRepository ratingRepository;
    @Mock
    private RatingMapper ratingMapper;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private EventMapper eventMapper;


    private RatingDto ratingDto;
    private Rating rating;
    private NewRatingDto newRatingDto;
    private Event event;
    private User user;
    private EventFullDto eventFullDto;
    private List<RatingDto> ratingDtoList = new ArrayList<>();

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(2L)
                .build();

        event = Event.builder()
                .id(1L)
                .initiator(user)
                .build();
        ratingDto = RatingDto.builder()
                .id(1L)
                .userId(1L)
                .eventId(1L)
                .assessment(4)
                .build();
        rating = Rating.builder()
                .user(User.builder()
                        .id(1L)
                        .build())
                .event(event)
                .build();
        newRatingDto = NewRatingDto.builder()
                .assessment(4)
                .build();
        eventFullDto = EventFullDto.builder()
                .id(1L)
                .initiator(UserShortDto.builder()
                        .id(2L)
                        .build())
                .build();
        ratingDtoList = new ArrayList<>();
        ratingDtoList.add(RatingDto.builder()
                .id(1L)
                .userId(1L)
                .eventId(event.getId())
                .assessment(4)
                .build());
        ratingDtoList.add(RatingDto.builder()
                .id(2L)
                .userId(2L)
                .eventId(event.getId())
                .assessment(5)
                .build());
        ratingDtoList.add(RatingDto.builder()
                .id(3L)
                .userId(3L)
                .eventId(event.getId())
                .assessment(3)
                .build());

    }

    @DisplayName("Сохранение рейтинга")
    @Test
    void addRatingDtoTest() {
        when(eventRepository.findById(eq(1L))).thenReturn(Optional.of(event));
        when(eventMapper.toFullDto(any(Event.class))).thenReturn(eventFullDto);
        when(ratingMapper.toModel(eq(1L), eq(1L), any(NewRatingDto.class))).thenReturn(rating);
        when(ratingRepository.save(any(Rating.class))).thenReturn(rating);
        when(ratingMapper.toDto(any(Rating.class))).thenReturn(ratingDto);

        assertEquals(ratingDto, ratingService.addRatingDto(1L, 1L, newRatingDto));

        verify(ratingMapper).toModel(eq(1L), eq(1L), any(NewRatingDto.class));
        verify(ratingRepository).save(any(Rating.class));
        verify(ratingMapper).toDto(any(Rating.class));
    }

    @DisplayName("Сохранение рейтинга когда пользователь является инициатором события")
    @Test
    void addRatingDto_WhenUserIsInitiator_EventTest() {
        when(eventRepository.findById(eq(1L))).thenReturn(Optional.of(event));
        when(eventMapper.toFullDto(any(Event.class))).thenReturn(eventFullDto);

        assertThrows(ConflictException.class, () -> ratingService.addRatingDto(2L, 1L, newRatingDto));

        verify(eventRepository).findById(eq(1L));
        verify(eventMapper).toFullDto(any(Event.class));
    }

    @DisplayName("Обновление рейтинга")
    @Test
    void updateRatingDtoTest() {

        NewRatingDto newRatingDto1 = NewRatingDto.builder()
                .assessment(5)
                .build();
        RatingDto ratingDto1 = RatingDto.builder()
                .id(1L)
                .userId(1L)
                .eventId(1L)
                .assessment(5)
                .build();

        when(ratingRepository.findById(eq(1L))).thenReturn(Optional.of(rating));
        when(ratingMapper.toDto(any(Rating.class))).thenReturn(ratingDto);
        when(ratingRepository.updateRatingByIdAndAssessment(eq(1L), eq(5))).thenReturn(1);
        when(ratingMapper.toDtoAfterUpdate(1L, 1L, newRatingDto1)).thenReturn(ratingDto1);

        assertEquals(ratingDto1, ratingService.updateRatingDto(1L, 1L, newRatingDto1));
    }

    @DisplayName("Обновление рейтинга когда пользователь не является создателем рейтинга")
    @Test
    void updateRatingDto_WhenUserIsNotAfterRatingTest() {
        when(ratingRepository.findById(eq(1L))).thenReturn(Optional.of(rating));
        when(ratingMapper.toDto(any(Rating.class))).thenReturn(ratingDto);

        assertThrows(ConflictException.class, () -> ratingService.updateRatingDto(2L, 1L, newRatingDto));
    }

    @DisplayName("Удаление рейтинга")
    @Test
    void deleteRatingDtoTest() {
        when(ratingRepository.findById(eq(1L))).thenReturn(Optional.of(rating));
        when(ratingMapper.toDto(any(Rating.class))).thenReturn(ratingDto);

        doNothing().when(ratingRepository).deleteById(eq(1L));

        ratingService.deleteRatingDto(1L, 1L);

        verify(ratingRepository).deleteById(1L);
    }

    @Test
    void getAvgAssessmentTest() {
        List<RatingDto> ratingDtoArrayList = new ArrayList<>(ratingDtoList);
        List<Rating> ratings = ratingMapper.toModelList(ratingDtoArrayList);

        when(ratingRepository.findAllByEventIdAndAssessmentNotNull(eq(1L)))
                .thenReturn(ratings);
        when(ratingMapper.toDtoList(eq(ratings))).thenReturn(ratingDtoArrayList);


        assertEquals(ratingService.getAvgAssessment(1L), 4.0);

    }
}