package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;
import ru.practicum.model.Compilation;

import java.util.List;

@Mapper(componentModel = "spring", uses = EventMapper.class)
public interface CompilationMapper {

    Compilation toModel(UpdateCompilationRequest updateCompilationRequest);

    Compilation toModel(CompilationDto compilationDto);

    CompilationDto toDto(Compilation compilation);

    List<CompilationDto> toListDto(List<Compilation> compilations);

    CompilationDto updateCompilation(@MappingTarget CompilationDto compilationDto, UpdateCompilationRequest updateCompilationRequest);
}