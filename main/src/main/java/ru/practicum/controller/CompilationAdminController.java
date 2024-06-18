package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;
import ru.practicum.service.impl.CompilationAdminService;

import static ru.practicum.util.Marker.OnCreate;
import static ru.practicum.util.Marker.OnUpdate;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/admin/compilations")
public class CompilationAdminController {
    private final CompilationAdminService compilationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto addCompilation(@Validated(OnCreate.class) @RequestBody UpdateCompilationRequest updateCompilationRequest) {
        System.err.println("Adding new compilation with request: " + updateCompilationRequest);
        CompilationDto result = compilationService.addCompilationDto(updateCompilationRequest);
        System.err.println("Added compilation: " + result);
        return result;
    }

    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilation(@PathVariable Long compId) {
        System.err.println("Deleting compilation with ID: " + compId);
        compilationService.deleteCompilationDto(compId);
        System.err.println("Deleted compilation with ID: " + compId);
    }

    @PatchMapping("/{compId}")
    public CompilationDto updateCompilation(@PathVariable Long compId, @Validated(OnUpdate.class) @RequestBody UpdateCompilationRequest updateCompilationRequest) {
        System.err.println("Updating compilation with ID: " + compId + " with request: " + updateCompilationRequest);
        CompilationDto result = compilationService.updateCompilation(compId, updateCompilationRequest);
        System.err.println("Updated compilation with ID: " + compId + ": " + result);
        return result;
    }
}
