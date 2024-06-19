package ru.practicum.dto.event;

import lombok.Data;
import org.hibernate.validator.constraints.Length;
import ru.practicum.model.Location;
import ru.practicum.util.NotOnlySpaces;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

@Data
public class NewEventDto {

    @NotOnlySpaces
    @NotNull(message = "Annotation cannot be null")
    @NotEmpty(message = "Annotation cannot be empty")
    @Length(min = 20, max = 2000)
    private String annotation;
    private Long category;

    @NotOnlySpaces
    @NotNull(message = "Description cannot be null")
    @NotEmpty(message = "Description cannot be empty")
    @Length(min = 20, max = 7000)
    private String description;

    private String eventDate;
    private Location location;
    private Boolean paid = false;

    @PositiveOrZero(message = "ParticipantLimit cannot be negative")

    private Integer participantLimit = 0;

    private Boolean requestModeration = true;

    @Length(min = 3, max = 120)
    private String title;
}
