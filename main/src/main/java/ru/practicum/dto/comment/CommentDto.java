package ru.practicum.dto.comment;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class CommentDto {
    private Long id;
    private String text;
    private Long author;
    private Long event;
    private boolean updated;
    private String created;
}
