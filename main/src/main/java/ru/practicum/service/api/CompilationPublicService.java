package ru.practicum.service.api;

import ru.practicum.dto.compilation.CompilationDto;

import java.util.List;

public interface CompilationPublicService {

    /**
     * Retrieves a compilation by its ID.
     *
     * @param compId the ID of the compilation to be retrieved
     * @return the CompilationDto containing details of the found compilation
     */
    CompilationDto findCompilationDtoById(Long compId);

    /**
     * Finds compilations based on their pinned status, with pagination support.
     *
     * @param pinned the pinned status of the compilations to find (true for pinned, false for not pinned)
     * @param from the starting index from which to retrieve compilations
     * @param size the maximum number of compilations to retrieve
     * @return a list of CompilationDto containing the compilations that match the pinned status
     */
    List<CompilationDto> findByPinned(Boolean pinned, int from, int size);
}
