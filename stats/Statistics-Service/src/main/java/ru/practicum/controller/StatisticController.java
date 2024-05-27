package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EndpointDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.service.StatisticService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class StatisticController {
    private final StatisticService statisticService;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public void createEndpointDto(@Valid @RequestBody EndpointDto endpointDto) {
        statisticService.createStatistic(endpointDto);
    }

    @GetMapping("/stats")
    public List<ViewStatsDto> getViewStatsDto(@RequestParam String start,
                                              @RequestParam String end,
                                              @RequestParam(required = false) List<String> uris,
                                              @RequestParam(defaultValue = "false") boolean unique) {

        return statisticService.getAllViewStatsDto(start, end, unique, uris);
    }
}