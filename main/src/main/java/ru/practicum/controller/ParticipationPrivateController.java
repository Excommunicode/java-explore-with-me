package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.registration.ParticipationRequestDto;
import ru.practicum.service.impl.ParticipationPrivateService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users/{userId}/requests")
public class ParticipationPrivateController {
    private final ParticipationPrivateService privateParticipationService;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto addEventRegistration(@PathVariable Long userId, @RequestParam Long eventId) {
        return privateParticipationService.addRequest(userId, eventId);
    }

    @GetMapping
    public List<ParticipationRequestDto> findInformationAboutUserRegistration(@PathVariable Long userId) {
        return privateParticipationService.findInformationAboutUserRegistration(userId);
    }

    @PatchMapping("/{requestId}/cancel")
    public ParticipationRequestDto cancelingYourRequest(@PathVariable Long userId, @PathVariable Long requestId) {
        return privateParticipationService.cancelingYourRequest(userId, requestId);
    }
}
