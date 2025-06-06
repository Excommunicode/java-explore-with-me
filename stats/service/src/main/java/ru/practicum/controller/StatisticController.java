package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.EndpointDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.service.StatisticService;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.constant.StatisticConstant.DATE_TIME_FORMATTER;

@RestController
@RequiredArgsConstructor
public class StatisticController {
    private final StatisticService statisticService;

    /**
     * Creates an endpoint DTO.
     *
     * @param endpointDto the data transfer object representing the endpoint information
     *                    The endpointDto object must have the following properties:
     *                    - app: String value representing the application
     *                    - uri: String value representing the URI
     *                    - ip: String value representing the IP address
     *                    - timestamp: String value representing the timestamp
     */
    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public void createEndpointDto(@Valid @RequestBody EndpointDto endpointDto) {
        statisticService.createStatistic(endpointDto);
    }

    /**
     * Retrieves a list of ViewStatsDto objects based on the specified start and end dates, uniqueness flag, and URI filter.
     *
     * @param start  The start date of the statistics range.
     * @param end    The end date of the statistics range.
     * @param uris   A list of URIs to filter the statistics by. Only the statistics for the specified URIs will be included in the result. (optional)
     * @param unique A boolean flag indicating whether to return unique statistics. Defaults to false if not specified.
     * @return A list of ViewStatsDto objects representing the view statistics for the specified period, uniqueness flag, and URI filter.
     */
    @GetMapping("/stats")
    public List<ViewStatsDto> getViewStatsDto(@RequestParam @DateTimeFormat(pattern = DATE_TIME_FORMATTER) LocalDateTime start,
                                              @RequestParam @DateTimeFormat(pattern = DATE_TIME_FORMATTER) LocalDateTime end,
                                              @RequestParam(required = false) List<String> uris,
                                              @RequestParam(defaultValue = "false") boolean unique) {

        return statisticService.getAllViewStatsDto(start, end, unique, uris);
    }
}