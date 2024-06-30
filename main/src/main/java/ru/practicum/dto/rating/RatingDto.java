package ru.practicum.dto.rating;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class RatingDto {
    private Long id;
    private Long userId;
    private Long eventId;
    private double assessment;
}