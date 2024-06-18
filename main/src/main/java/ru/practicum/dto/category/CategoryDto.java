package ru.practicum.dto.category;

import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import ru.practicum.util.NotOnlySpaces;

import javax.validation.constraints.NotNull;

@Data
@Builder(toBuilder = true)
public class CategoryDto {
    private Long id;
    @NotOnlySpaces
    @NotNull(message = "Name cannot be null")
    @Length(max = 50)
    private String name;
}