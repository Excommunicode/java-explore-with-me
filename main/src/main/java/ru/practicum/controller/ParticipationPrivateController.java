package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.registration.ParticipationRequestDto;
import ru.practicum.service.api.ParticipationPrivateService;

import java.util.List;

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
