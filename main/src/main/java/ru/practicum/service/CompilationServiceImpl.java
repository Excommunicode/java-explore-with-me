package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;
import ru.practicum.exceptiion.NotFoundException;
import ru.practicum.mapper.CompilationMapper;
import ru.practicum.repository.CompilationRepository;
import ru.practicum.service.api.CompilationAdminService;
import ru.practicum.service.api.CompilationPublicService;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
public class CompilationServiceImpl implements CompilationAdminService, CompilationPublicService {
    private final CompilationRepository compilationRepository;
    private final CompilationMapper compilationMapper;


    @Transactional
    @Override
    public CompilationDto addCompilationDto(UpdateCompilationRequest updateCompilationRequest) {
        log.debug("Adding new compilation with request: {}", updateCompilationRequest);

        CompilationDto compilationDto = compilationMapper.toDto(
                compilationRepository.save(compilationMapper.toModel(updateCompilationRequest)));

        log.info("Added new compilation: {}", compilationDto);
        return compilationDto;
    }

    @Transactional
    @Override
    public void deleteCompilationDto(Long compId) {
        log.debug("Deleting compilation with id: {}", compId);

        compilationRepository.deleteById(compId);

        log.info("Deleted compilation with id: {}", compId);
    }

    @Transactional
    @Override
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateCompilationRequest) {
        log.debug("Updating compilation with id: {} and request: {}", compId, updateCompilationRequest);

        CompilationDto compilationDto = compilationMapper.updateCompilation(findCompilationById(compId), updateCompilationRequest);
        CompilationDto updatedCompilationDto = compilationMapper.toDto(compilationRepository.save(compilationMapper.toModel(compilationDto)));

        log.info("Updated compilation: {}", updatedCompilationDto);
        return updatedCompilationDto;
    }

    @Override
    public CompilationDto findCompilationDtoById(Long compId) {
        log.debug("Finding compilation with id: {}", compId);

        CompilationDto compilationDto = findCompilationById(compId);

        log.info("Found compilation: {}", compilationDto);
        return compilationDto;
    }

    @Override
    public List<CompilationDto> findByPinned(Boolean pinned, int from, int size) {
        log.debug("Finding compilations with pinned: {}, from: {}, size: {}", pinned, from, size);

        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size, sort);

        List<CompilationDto> compilationDtoList = pinned != null ? findAllCompilationByPinned(pinned, pageable) :
                findAllCompilation(pageable);

        if (compilationDtoList == null || compilationDtoList.isEmpty()) {
            log.warn("No compilations found");
            return Collections.emptyList();
        }

        log.info("Found compilations: {}", compilationDtoList);
        return compilationDtoList;
    }

    private CompilationDto findCompilationById(Long compId) {
        return compilationMapper.toDto(compilationRepository.findById(compId)
                .orElseThrow(() -> {
                    log.error("Compilation with id {} not found", compId);
                    return new NotFoundException(String.format("Compilation with id %s not found", compId));
                }));
    }

    private List<CompilationDto> findAllCompilationByPinned(Boolean pinned, Pageable pageable) {
        return compilationMapper.toListDto(compilationRepository.findAllByPinned(pinned, pageable));
    }

    private List<CompilationDto> findAllCompilation(Pageable pageable) {
        return compilationMapper.toListDto(compilationRepository.findAll(pageable).getContent());
    }
}