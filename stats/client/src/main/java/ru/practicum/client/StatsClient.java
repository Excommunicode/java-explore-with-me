package ru.practicum.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.dto.EndpointDto;
import ru.practicum.dto.ViewStatsDto;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class StatsClient {
    private final RestTemplate restTemplate;


    @Autowired
    public StatsClient(@Value("${stat.server.url}") String serverUrl, RestTemplateBuilder builder) {
        restTemplate = builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                .build();
    }

    public void postStats(EndpointDto endpointDto) {
        restTemplate.postForLocation("/hit", endpointDto);
    }

    public List<ViewStatsDto> getStats(String start, String end, List<String> uris, boolean unique) {

        String url = String.format("/stats?start=%s&end=%s&uris=%s&unique=%s", start, end, uris, unique);

        ResponseEntity<ViewStatsDto[]> response = restTemplate.getForEntity(url, ViewStatsDto[].class);
        return (response.getBody() != null) ? Arrays.asList(response.getBody()) : Collections.emptyList();

    }
}