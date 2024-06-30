package ru.practicum.dto.rating;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.PositiveOrZero;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class NewRatingDto {
    @PositiveOrZero(message = "Assessment must be zero or positive")
    @Max(value = 5, message = "Assessment must be less than or equal to 5")
    int assessment;
}