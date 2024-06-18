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
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.Event;
import ru.practicum.repository.CompilationRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.service.impl.CompilationAdminService;
import ru.practicum.service.impl.CompilationPublicService;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
public class CompilationServiceImpl implements CompilationAdminService, CompilationPublicService {
    private final CompilationRepository compilationRepository;
    private final CompilationMapper compilationMapper;
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;


    @Transactional
    @Override
    public CompilationDto addCompilationDto(UpdateCompilationRequest updateCompilationRequest) {
        Set<Event> allByIdIn;
        if (updateCompilationRequest.getEvents() != null) {
            allByIdIn = eventRepository.findAllByIdIn(updateCompilationRequest.getEvents());
        } else {
            allByIdIn = Collections.emptySet();
        }
        return compilationMapper.toDto(
                compilationRepository.save(compilationMapper.toModel(updateCompilationRequest, allByIdIn)));
    }

    @Transactional
    @Override
    public void deleteCompilationDto(Long compId) {
        compilationRepository.deleteById(compId);
    }

    @Transactional
    @Override
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateCompilationRequest) {
        CompilationDto compilationDto = findCompilationById(compId);
        Set<Event> allByIdIn;
        if (updateCompilationRequest.getEvents() != null) {
            allByIdIn = eventRepository.findAllByIdIn(updateCompilationRequest.getEvents());
        } else {
            allByIdIn = Collections.emptySet();
        }

        compilationDto.setPinned(updateCompilationRequest.isPinned());

        if (updateCompilationRequest.getTitle() != null) {
            compilationDto.setTitle(updateCompilationRequest.getTitle());
        }

        if (allByIdIn != null) {
            compilationDto.setEvents(eventMapper.toShortDto(allByIdIn));
        }

        return compilationMapper.toDto(
                compilationRepository.save(compilationMapper.toModel(compilationDto)));
    }

    @Override
    public CompilationDto findCompilationDtoById(Long compId) {
        return findCompilationById(compId);
    }

    @Override
    public List<CompilationDto> findByPinned(Boolean pinned, int from, int size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(from, size, sort);
        List<CompilationDto> listDto;
        if (pinned != null) {
            listDto = compilationMapper.toListDto(compilationRepository.findAllByPinned(pinned, pageable));
        } else {
            listDto = compilationMapper.toListDto(compilationRepository.findAll(pageable).getContent());
        }

        if (listDto.isEmpty() || listDto == null) {
            return Collections.emptyList();
        }
        return listDto;
    }

    private CompilationDto findCompilationById(Long compId) {
        return compilationMapper.toDto(compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation not found")));
    }
}