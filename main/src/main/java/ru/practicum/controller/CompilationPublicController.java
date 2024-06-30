package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.service.impl.CompilationPublicService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

import static ru.practicum.constant.UserConstant.INITIAL_X;
import static ru.practicum.constant.UserConstant.LIMIT;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/compilations")
public class CompilationPublicController {
    private final CompilationPublicService compilationPublicService;

    @GetMapping
    public List<CompilationDto> findByPinned(@RequestParam(required = false) Boolean pinned,
                                             @PositiveOrZero @RequestParam(defaultValue = INITIAL_X) int from,
                                             @Positive @RequestParam(defaultValue = LIMIT) int size) {
        return compilationPublicService.findByPinned(pinned, from, size);
    }

    @GetMapping("/{compId}")
    public CompilationDto findCompilationDtoById(@PathVariable Long compId) {
        return compilationPublicService.findCompilationDtoById(compId);
    }
}