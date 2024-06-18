package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.practicum.dto.event.*;
import ru.practicum.enums.StateAction;
import ru.practicum.model.Event;
import ru.practicum.enums.State;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static ru.practicum.constant.EventConstant.DATE_TIME_FORMATTER;
import static ru.practicum.enums.StateAction.*;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EventMapper {

    @Mappings({
            @Mapping(target = "category.id", source = "category"),
            @Mapping(target = "eventDate", source = "eventDate", dateFormat = DATE_TIME_FORMATTER)
    })
    Event toModel(NewEventDto newEventDto);

    @Mapping(target = "eventDate", source = "eventDate", dateFormat = DATE_TIME_FORMATTER)
    EventFullDto toFullDto(Event event);

    @Mapping(target = "eventDate", source = "eventDate", dateFormat = DATE_TIME_FORMATTER)
    Event toModelFromFullDto(EventFullDto eventFullDto);

    List<EventFullDto> toDtoList(List<Event> events);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "category.id", source = "newEventDto.category"),
            @Mapping(target = "eventDate", source = "newEventDto.eventDate", dateFormat = DATE_TIME_FORMATTER),
            @Mapping(target = "initiator.id", source = "userId"),
            @Mapping(target = "state", source = "state")
    })
    Event toEvent(NewEventDto newEventDto, Long userId, State state, LocalDateTime createdOn);

    @Mappings({
            @Mapping(target = "eventDate", source = "event.eventDate", dateFormat = DATE_TIME_FORMATTER),
            @Mapping(target = "stateAction", source = "action")
    })
    UpdateEventUserRequest toUpdateDto(Event event, StateAction action);

    default UpdateEventUserRequest toUpdateWhen(Event event) {
        switch (event.getState()) {
            case PUBLISHED:
                return toUpdateDto(event, PUBLISH_EVENT);
            case PENDING:
                return toUpdateDto(event, SEND_TO_REVIEW);
            case CANCELED:
                return toUpdateDto(event, CANCEL_REVIEW);
            default:
                return toUpdateDto(event, null);
        }
    }


    @Mapping(target = "eventDate", source = "eventDate", dateFormat = DATE_TIME_FORMATTER)
    UpdateEventUserRequestOutput toUpdateDtoOutput(Event event);


    Set<EventShortDto> toShortDto(Set<Event> eventFullDto);
}
