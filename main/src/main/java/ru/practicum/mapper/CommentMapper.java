package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.NewCommentDto;
import ru.practicum.model.Comment;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.constant.EventConstant.DATE_TIME_FORMATTER;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    @Mappings({
            @Mapping(target = "author.id", source = "userId"),
            @Mapping(target = "event.id", source = "eventId")
    })
    Comment toModel(Long userId, Long eventId, NewCommentDto newCommentDto, LocalDateTime created);

    @Mappings({
            @Mapping(target = "author", source = "author.id"),
            @Mapping(target = "event", source = "event.id"),
            @Mapping(target = "created", source = "created", dateFormat = DATE_TIME_FORMATTER)

    })
    CommentDto toDto(Comment comment);

    List<CommentDto> toListDto(List<Comment> comments);
}