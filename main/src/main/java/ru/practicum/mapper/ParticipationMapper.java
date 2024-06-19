package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import ru.practicum.dto.registration.ParticipationRequestDto;
import ru.practicum.enums.ParticipationRequestStatus;
import ru.practicum.model.ParticipationRequest;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.constant.EventConstant.DATE_TIME_FORMATTER;

@Mapper(componentModel = "spring")
public interface ParticipationMapper {

    @Mappings({
            @Mapping(target = "event", source = "event.id"),
            @Mapping(target = "requester", source = "requester.id"),
            @Mapping(target = "created", source = "created", dateFormat = DATE_TIME_FORMATTER)
    })
    ParticipationRequestDto toDto(ParticipationRequest participationRequest);

    @Mappings({
            @Mapping(target = "requester.id", source = "userId"),
            @Mapping(target = "event.id", source = "event"),
            @Mapping(target = "created", source = "created", dateFormat = DATE_TIME_FORMATTER)
    })
    ParticipationRequest toModel(Long userId, Long event, LocalDateTime created, ParticipationRequestStatus status);

    List<ParticipationRequestDto> toListDto(List<ParticipationRequest> events);
}