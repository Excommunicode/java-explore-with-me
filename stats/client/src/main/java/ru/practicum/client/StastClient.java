package ru.practicum.client;

import org.springframework.web.client.RestTemplate;

public class StastClient {
    private final RestTemplate restTemplate;

    public StastClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

}
