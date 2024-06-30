package ru.practicum.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.practicum.dto.rating.NewRatingDto;
import ru.practicum.dto.rating.RatingDto;
import ru.practicum.service.impl.RatingPrivateService;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@WebMvcTest(RatingPrivateController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class RatingPrivateControllerTest {
    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    @MockBean
    private RatingPrivateService ratingPrivateService;
    private NewRatingDto newRatingDto;
    private RatingDto ratingDto;

    @BeforeEach
    void setUp() {

        newRatingDto = NewRatingDto.builder()
                .assessment(4)
                .build();

        ratingDto = RatingDto.builder()
                .id(1L)
                .userId(1L)
                .eventId(1L)
                .assessment(4)
                .build();


    }

    @Test
    @SneakyThrows
    void addRatingDtoTest() {
        when(ratingPrivateService.addRatingDto(eq(1L), eq(1L), any(NewRatingDto.class)))
                .thenReturn(ratingDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/users/{userId}/rating/{eventId}", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newRatingDto)))
                .andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(ratingDto)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(ratingDto.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.userId").value(ratingDto.getUserId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.eventId").value(ratingDto.getEventId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.assessment").value(ratingDto.getAssessment()));
    }

    @Test
    @SneakyThrows
    void updateRatingDtoTest() {
        ratingDto.setAssessment(5);

        when(ratingPrivateService.updateRatingDto(eq(1L), eq(1L), any(NewRatingDto.class)))
                .thenReturn(ratingDto);

        mockMvc.perform(MockMvcRequestBuilders.patch("/users/{userId}/rating/{ratingId}", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newRatingDto)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @SneakyThrows
    void deleteRatingDto() {
        mockMvc.perform(MockMvcRequestBuilders.delete("/users/{userId}/rating/{ratingId}", 1L, 1L))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }
}