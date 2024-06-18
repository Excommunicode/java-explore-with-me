package ru.practicum.service.impl;

import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.UpdateEventAdminRequest;

import java.util.List;

public interface EventAdminService {

    EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateEventAdminRequest);

    List<EventFullDto> findEventForAdmin(List<Long> users, List<String> states, List<Long> categories,
                                         String rangeStart, String rangeEnd, Integer from, Integer size);
}
