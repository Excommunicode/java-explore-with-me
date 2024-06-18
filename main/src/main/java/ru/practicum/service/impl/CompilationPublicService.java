package ru.practicum.service.impl;

import ru.practicum.dto.compilation.CompilationDto;

import java.util.List;

public interface CompilationPublicService {
    CompilationDto findCompilationDtoById(Long compId);

    List<CompilationDto> findByPinned(Boolean pinned, int from, int size);
}