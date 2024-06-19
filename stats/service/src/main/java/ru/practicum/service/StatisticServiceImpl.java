package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.EndpointDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.exception.BadRequestException;
import ru.practicum.mapper.StatisticMapper;
import ru.practicum.model.StatisticProjection;
import ru.practicum.repository.StatisticRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
public class StatisticServiceImpl implements StatisticService {
    private final StatisticRepository statisticRepository;
    private final StatisticMapper statisticMapper;

    @Transactional
    @Override
    public void createStatistic(EndpointDto endpointDto) {
        log.debug("Creating statistic for endpoint: {}", endpointDto);
        statisticRepository.save(statisticMapper.toEndpointDto(endpointDto));
        log.info("Statistic created successfully for endpoint: {}", endpointDto);
    }

    @Override
    public List<ViewStatsDto> getAllViewStatsDto(LocalDateTime start, LocalDateTime end, boolean unique, List<String> uri) {
        log.debug("Requesting view statistics from {} to {}, unique: {}, for URIs: {}", start, end, unique, uri);
        checkTime(start, end);
        List<String> stringList = uri.parallelStream()
                .map(s -> s.replace("[", "").replace("]", ""))
                .collect(Collectors.toList());

        List<StatisticProjection> allByTimestampBetween;
        if (uri == null || uri.isEmpty()) {
            allByTimestampBetween = unique ? statisticRepository.findAllByTimestampBetween(start, end)
                    : statisticRepository.findAllStatisticProjectionByTimestampBetween(start, end);
        } else {
            allByTimestampBetween = unique ?
                    statisticRepository.findAllStatisticProjectionByTimestampBetweenAndUri(start, end, stringList)
                    : statisticRepository.findAllStatisticProjectionByTimestampBetweenAndUriIn(start, end, stringList);
        }

        List<ViewStatsDto> results = findUnique(allByTimestampBetween);
        log.info("Retrieved statistics from {} to {}: {}", start, end, results.size());
        return results;
    }

    private List<ViewStatsDto> findUnique(List<StatisticProjection> statisticProjections) {
        Map<String, ViewStatsDto> viewStatsDtoMap = new LinkedHashMap<>();

        for (StatisticProjection statisticProjection : statisticProjections) {
            String key = statisticProjection.getApp() + "-" + statisticProjection.getUri();

            if (!viewStatsDtoMap.containsKey(key)) {
                viewStatsDtoMap.put(key, ViewStatsDto.builder()
                        .app(statisticProjection.getApp())
                        .uri(statisticProjection.getUri())
                        .hits(1)
                        .build());
            } else {
                viewStatsDtoMap.get(key).setHits(viewStatsDtoMap.get(key).getHits() + 1);
            }
        }
        return viewStatsDtoMap.values().stream()
                .sorted(Comparator.comparingLong(ViewStatsDto::getHits).reversed())
                .collect(Collectors.toList());
    }

    private void checkTime(LocalDateTime start, LocalDateTime end) {
        if (end.isBefore(start)) {
            throw new BadRequestException("End cannot early start");
        }
    }
}
