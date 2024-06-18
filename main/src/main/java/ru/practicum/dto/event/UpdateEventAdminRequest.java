package ru.practicum.dto.event;

import lombok.Data;
import org.hibernate.validator.constraints.Length;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.enums.StateAction;
import ru.practicum.model.Location;

@Data
public class UpdateEventAdminRequest {

    @Length(min = 20, max = 2000)
    private String annotation;

    private CategoryDto categoryDto;

    @Length(min = 20, max = 7000)
    private String description;

    private String eventDate;
    private Location location;
    private Boolean paid;
    private Integer participantLimit;
    private Boolean requestModeration;
    private StateAction stateAction;

    @Length(min = 3, max = 120)
    private String title;
}
