package ru.practicum.dto.compilation;

import lombok.Builder;
import lombok.Data;
import ru.practicum.util.NotOnlySpaces;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

import static ru.practicum.util.Marker.OnCreate;
import static ru.practicum.util.Marker.OnUpdate;

@Data
@Builder(toBuilder = true)
public class UpdateCompilationRequest {
    private List<Long> events;
    private boolean pinned;
    @NotOnlySpaces(groups = OnCreate.class)
    @NotNull(message = "Title cannot be null", groups = OnCreate.class)
    @Size(max = 50, groups = {OnUpdate.class, OnCreate.class})
    private String title;
}
