package ru.practicum.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class EndpointDto {
    private String app;
    private String uri;
    private String ip;
    private String timestamp;
}
