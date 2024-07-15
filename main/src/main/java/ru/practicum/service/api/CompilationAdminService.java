package ru.practicum.service.api;

import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;

public interface CompilationAdminService {
    /**
     * Adds a new compilation based on the provided request.
     *
     * @param updateCompilationRequest the request containing the details for the new compilation
     * @return the newly created CompilationDto with details of the added compilation
     */
    CompilationDto addCompilationDto(UpdateCompilationRequest updateCompilationRequest);

    /**
     * Deletes a compilation identified by its ID.
     *
     * @param compId the ID of the compilation to be deleted
     */
    void deleteCompilationDto(Long compId);

    /**
     * Updates an existing compilation identified by its ID using the provided request details.
     *
     * @param compId the ID of the compilation to update
     * @param updateCompilationRequest the request containing the new details for the compilation
     * @return the updated CompilationDto with the modified details
     */
    CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateCompilationRequest);
}
