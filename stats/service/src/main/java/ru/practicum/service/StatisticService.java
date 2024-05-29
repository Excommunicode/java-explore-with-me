package ru.practicum.service;

import ru.practicum.dto.EndpointDto;
import ru.practicum.dto.ViewStatsDto;

import java.util.List;

public interface StatisticService {
    /**
     * Creates a statistic record for the specified endpoint.
     *
     * @param endpointDto the DTO object representing the endpoint information
     */
    void createStatistic(EndpointDto endpointDto);

    /**
     * Retrieves a list of ViewStatsDto objects based on the specified start and end dates, uniqueness flag, and URI filter.
     *
     * @param start The start date of the statistics range.
     * @param end The end date of the statistics range.
     * @param unique A boolean flag indicating whether to return unique statistics.
     * @param uri A list of URIs to filter the statistics by. Only the statistics for the specified URIs will be included in the result.
     * @return A list of ViewStatsDto objects representing the view statistics for the specified period, uniqueness flag, and URI filter.
     */
    List<ViewStatsDto> getAllViewStatsDto(String  start, String end, boolean unique, List<String> uri);
}
