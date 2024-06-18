package ru.practicum.service.impl;

import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;

public interface CompilationAdminService {
    CompilationDto addCompilationDto(UpdateCompilationRequest updateCompilationRequest);

    void deleteCompilationDto(Long compId);

    CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateCompilationRequest);
}
