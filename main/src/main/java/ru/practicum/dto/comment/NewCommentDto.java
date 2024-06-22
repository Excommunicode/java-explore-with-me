package ru.practicum.dto.comment;

import lombok.Data;
import ru.practicum.util.NotOnlySpaces;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class NewCommentDto {

    @NotOnlySpaces
    @NotNull(message = "Text cannot be null")
    @Size(min = 1, max = 1024, message = "comment must be between 1 and 1024 characters")
    private String text;
}
