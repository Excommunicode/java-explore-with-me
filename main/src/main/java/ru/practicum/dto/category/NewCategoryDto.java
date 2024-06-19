package ru.practicum.dto.category;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class NewCategoryDto {
    private String name;
}
