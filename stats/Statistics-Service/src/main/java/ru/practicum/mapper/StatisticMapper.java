package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import ru.practicum.dto.EndpointDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.model.Statistic;
import ru.practicum.model.StatisticProjection;

import java.util.List;

import static ru.practicum.constant.StatisticConstant.DATE_TIME_FORMATTER;

@Mapper(componentModel = "spring")
public interface StatisticMapper {
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "timestamp", source = "timestamp", dateFormat = DATE_TIME_FORMATTER)
    })
    Statistic toEndpointDto(EndpointDto endpointDto);

//    List<ViewStatsDto> toViewDto(List<StatisticProjection> viewStatsDto);
    @Mapping(target = "id", ignore = true)
    List<ViewStatsDto> toViewDto(List<Statistic> viewStatsDto);

}
