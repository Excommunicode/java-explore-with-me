package ru.practicum.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.dto.EndpointDto;
import ru.practicum.dto.ViewStatsDto;

import java.util.List;

@Service
public class StastClient {
    private static final String BASE_URL = "http://localhost:8080";
    private static final String POST_ENDPOINT = "/hit";
    private static final String GET_ENDPOINT = "/stats?start=%s&end=%s&uris=%s&unique=%s";
    private final RestTemplate restTemplate;

    @Autowired
    public StastClient(@Value("${STATS_SERVER_URL}") String serverUrl, RestTemplateBuilder builder) {

        restTemplate = builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                .requestFactory(HttpComponentsClientHttpRequestFactory.class)
                .build();
    }

    public void createEndpointDto(EndpointDto endpointDto) {
        restTemplate.postForEntity(POST_ENDPOINT, endpointDto, Void.class);
    }

    public List<ViewStatsDto> getViewStatsDto(String start, String end, List<String> uris, boolean unique) {
        String uriString = String.join(",", uris);
        String url = String.format(GET_ENDPOINT, start, end, uriString, unique);
        ResponseEntity<List<ViewStatsDto>> response = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<ViewStatsDto>>() {
                });
        return response.getBody();
    }

}